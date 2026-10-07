package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.ProductoValidador;
import ni.edu.uam.fact_app.util.ResultadoValidacion;
import ni.edu.uam.fact_app.util.Validador;
import ni.edu.uam.fact_app.util.Mensajes;
import java.sql.SQLException;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;
    @FXML private Label lblEstadoImagen;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, String> colActivo;

    private static final String FILTRO_TODOS = "Todos";
    private static final String FILTRO_ACTIVOS = "Activos";
    private static final String FILTRO_INACTIVOS = "Inactivos";

    private static final Categoria CATEGORIA_TODAS =
            new Categoria(null, "Todas las categorías", true);

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final Validador<Producto> validador = new ProductoValidador(productoDAO);

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    private FilteredList<Producto> productosFiltrados;

    private Producto productoSeleccionado;
    private String rutaImagen;

    @FXML
    private void initialize() {
        configurarColumnas();
        configurarComboCategorias();
        configurarSeleccionTabla();
        configurarBuscador();
        configurarFiltroEstado();
        configurarFiltroCategoria();
        chkActivo.setSelected(true);

        productosFiltrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(productosFiltrados);

        try {
            recargar();
        } catch (SQLException ex) {
            reportarErrorBD(ex, "cargar los productos");
        }
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCategoria() != null
                        ? cd.getValue().getCategoria().getNombre() : ""));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isActivo() ? "Sí" : "No"));
    }

    private void configurarComboCategorias() {
        try {
            List<Categoria> activas = categoriaDAO.listar().stream()
                    .filter(Categoria::isActivo)
                    .toList();
            cmbCategoria.setItems(FXCollections.observableArrayList(activas));
        } catch (SQLException ex) {
            reportarErrorBD(ex, "cargar las categorías");
        }
    }

    private void configurarSeleccionTabla() {
        tblProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, viejo, nuevo) -> {
                    if (nuevo != null) cargarEnFormulario(nuevo);
                });
    }

    private void configurarBuscador() {
        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());
    }

    private void configurarFiltroEstado() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                FILTRO_TODOS, FILTRO_ACTIVOS, FILTRO_INACTIVOS));
        cmbFiltroEstado.setValue(FILTRO_TODOS);
        cmbFiltroEstado.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());
    }

    private void configurarFiltroCategoria() {
        ObservableList<Categoria> items = FXCollections.observableArrayList();
        items.add(CATEGORIA_TODAS);
        try {
            items.addAll(categoriaDAO.listar());
        } catch (SQLException ex) {
            reportarErrorBD(ex, "cargar las categorías");
        }
        cmbFiltroCategoria.setItems(items);
        cmbFiltroCategoria.setValue(CATEGORIA_TODAS);
        cmbFiltroCategoria.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null
                ? "" : txtBuscar.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();
        Categoria categoriaFiltro = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(p -> {
            if (!texto.isEmpty()) {
                boolean matchTexto =
                        (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(texto))
                                || (p.getNombre() != null && p.getNombre().toLowerCase().contains(texto))
                                || (p.getCategoria() != null && p.getCategoria().getNombre() != null
                                && p.getCategoria().getNombre().toLowerCase().contains(texto));
                if (!matchTexto) return false;
            }

            // 2) Estado
            if (FILTRO_ACTIVOS.equals(estado) && !p.isActivo()) return false;
            if (FILTRO_INACTIVOS.equals(estado) && p.isActivo()) return false;

            // 3) Categoría exacta (si no es el sentinel "Todas")
            if (categoriaFiltro != null && categoriaFiltro.getId() != null) {
                if (p.getCategoria() == null
                        || p.getCategoria().getId() == null
                        || !p.getCategoria().getId().equals(categoriaFiltro.getId())) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
    }

    private void recargar() throws SQLException {
        productos.setAll(productoDAO.listar());
    }
    private void cargarEnFormulario(Producto p) {
        productoSeleccionado = p;
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(p.getPrecioVenta() == null ? "" : p.getPrecioVenta().toPlainString());
        txtExistencia.setText(String.valueOf(p.getExistencia()));
        cmbCategoria.setValue(p.getCategoria());
        chkActivo.setSelected(p.isActivo());

        rutaImagen = p.getRutaImagen();
        cargarImagenSiExiste(rutaImagen);
    }

    private void cargarImagenSiExiste(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            imgProducto.setImage(null);
            lblEstadoImagen.setText("");
            return;
        }
        try {
            Image img = new Image(ruta, true);
            if (img.isError()) {
                imgProducto.setImage(null);
                lblEstadoImagen.setText("Imagen no disponible (archivo no encontrado)");
            } else {
                imgProducto.setImage(img);
                lblEstadoImagen.setText("");
            }
        } catch (Exception ex) {
            imgProducto.setImage(null);
            lblEstadoImagen.setText("Imagen no disponible: " + ex.getMessage());
        }
    }

    @FXML
    private void guardar() {
        Producto p;
        try {
            p = construirDesdeFormulario(null);
        } catch (NumberFormatException ex) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.ERROR, ex.getMessage());
            return;
        }

        ResultadoValidacion res;
        try {
            res = validador.validar(p);
        } catch (SQLException ex) {
            reportarErrorBD(ex, "validar el producto");
            return;
        }

        if (!res.isValido()) {
            mostrarValidacionFallida(res);
            return;
        }

        ejecutarSeguro(() -> productoDAO.guardar(p), "Producto agregado correctamente.");
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.WARNING,
                    "Seleccione un producto de la tabla primero.");
            return;
        }

        Producto p;
        try {
            p = construirDesdeFormulario(productoSeleccionado.getId());
        } catch (NumberFormatException ex) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.ERROR, ex.getMessage());
            return;
        }

        ResultadoValidacion res;
        try {
            res = validador.validar(p);
        } catch (SQLException ex) {
            reportarErrorBD(ex, "validar el producto");
            return;
        }

        if (!res.isValido()) {
            mostrarValidacionFallida(res);
            return;
        }

        ejecutarSeguro(() -> productoDAO.actualizar(p), "Producto actualizado correctamente.");
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.WARNING,
                    "Seleccione un producto de la tabla primero.");
            return;
        }
        if (!Mensajes.confirmar(txtCodigo,
                "¿Eliminar el producto '" + productoSeleccionado.getNombre() + "'?")) {
            return;
        }
        int id = productoSeleccionado.getId();
        ejecutarSeguro(() -> productoDAO.eliminar(id), "Producto eliminado correctamente.");
    }

    @FunctionalInterface
    private interface OperacionDAO {
        void ejecutar() throws SQLException;
    }

    private void ejecutarSeguro(OperacionDAO accion, String mensajeExito) {
        try {
            accion.ejecutar();
        } catch (SQLException ex) {
            reportarErrorBD(ex, "guardar los cambios");
            return;
        } catch (RuntimeException ex) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.ERROR,
                    "Operación fallida: " + ex.getMessage());
            return;
        }

        if (mensajeExito != null) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.INFORMATION, mensajeExito);
        }
        limpiar();
        try {
            recargar();
        } catch (SQLException ex) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.WARNING,
                    "La operación se realizó, pero no se pudo actualizar la tabla.\n"
                            + "Verifique su conexión a la base de datos.");
            System.err.println("[BD] Error al recargar productos: " + ex.getMessage());
        }
    }

    private Producto construirDesdeFormulario(Integer id) {
        BigDecimal precio;
        int existencia;

        try {
            precio = txtPrecio.getText().isBlank()
                    ? BigDecimal.ZERO
                    : new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException ex) {
            txtPrecio.requestFocus();
            throw new NumberFormatException("El precio debe ser un número válido.");
        }

        try {
            existencia = txtExistencia.getText().isBlank()
                    ? -1
                    : Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException ex) {
            txtExistencia.requestFocus();
            throw new NumberFormatException("La existencia debe ser un número entero.");
        }

        return new Producto(
                id,
                txtCodigo.getText() == null ? "" : txtCodigo.getText().trim(),
                txtNombre.getText() == null ? "" : txtNombre.getText().trim(),
                cmbCategoria.getValue(),
                precio,
                existencia,
                rutaImagen,
                chkActivo.isSelected()
        );
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
            lblEstadoImagen.setText("");
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    @FXML
    private void limpiar() {
        productoSeleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        lblEstadoImagen.setText("");
        rutaImagen = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    private void mostrarValidacionFallida(ResultadoValidacion res) {
        Mensajes.mostrar(txtCodigo, Alert.AlertType.WARNING, res.getMensaje());
        enfocarCampo(res.getCampo());
    }

    private void enfocarCampo(ResultadoValidacion.Campo campo) {
        switch (campo) {
            case CODIGO      -> txtCodigo.requestFocus();
            case NOMBRE      -> txtNombre.requestFocus();
            case CATEGORIA   -> cmbCategoria.requestFocus();
            case PRECIO      -> txtPrecio.requestFocus();
            case EXISTENCIA  -> txtExistencia.requestFocus();
            case NINGUNO     -> { /* nada */ }
        }
    }

    private void reportarErrorBD(SQLException ex, String contexto) {
        System.err.println("[BD] Error al " + contexto + ": " + ex.getMessage());
        Mensajes.mostrar(txtCodigo, Alert.AlertType.ERROR,
                "No fue posible completar la operación.\n"
                        + "Verifique su conexión a la base de datos e intente nuevamente.");
    }

}