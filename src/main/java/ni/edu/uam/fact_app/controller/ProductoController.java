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

    /** Sentinel para el ComboBox de categorías: id == null significa "sin filtro". */
    private static final Categoria CATEGORIA_TODAS =
            new Categoria(null, "Todas las categorías", true);

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final Validador<Producto> validador = new ProductoValidador(productoDAO);

    /** Fuente persistente para el TableView. No se recrea; solo se refresca con setAll(). */
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    /** Vista filtrada que alimenta al TableView (productos → productosFiltrados → TableView). */
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

        recargar();
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
        List<Categoria> activas = categoriaDAO.listar().stream()
                .filter(Categoria::isActivo)
                .toList();
        cmbCategoria.setItems(FXCollections.observableArrayList(activas));
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
        items.addAll(categoriaDAO.listar()); // todas, no solo activas
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

    /** Refresca la ObservableList persistente en lugar de reemplazar items del TableView. */
    private void recargar() {
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

        ResultadoValidacion res = validador.validar(p);
        if (!res.isValido()) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.WARNING, res.getMensaje());
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

        ResultadoValidacion res = validador.validar(p);
        if (!res.isValido()) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.WARNING, res.getMensaje());
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

    private void ejecutarSeguro(Runnable accion, String mensajeExito) {
        try {
            accion.run();
            if (mensajeExito != null) {
                Mensajes.mostrar(txtCodigo, Alert.AlertType.INFORMATION, mensajeExito);
            }
            limpiar();
            recargar();
        } catch (RuntimeException ex) {
            Mensajes.mostrar(txtCodigo, Alert.AlertType.ERROR, "Operación fallida: " + ex.getMessage());
        }
    }

    private Producto construirDesdeFormulario(Integer id) {
        BigDecimal precio;
        int existencia;
        try {
            precio = txtPrecio.getText().isBlank()
                    ? BigDecimal.ZERO
                    : new BigDecimal(txtPrecio.getText().trim());
            existencia = txtExistencia.getText().isBlank()
                    ? -1
                    : Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Precio o existencia deben ser numéricos.");
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
}