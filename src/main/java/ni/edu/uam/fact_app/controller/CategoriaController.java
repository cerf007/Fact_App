package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.util.CategoriaValidador;
import ni.edu.uam.fact_app.util.Mensajes;
import ni.edu.uam.fact_app.util.ResultadoValidacion;
import ni.edu.uam.fact_app.util.Validador;
import java.sql.SQLException;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActivo;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActivo;

    private final CategoriaDAO dao = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final Validador<Categoria> validador = new CategoriaValidador(dao);

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    private Categoria seleccionada;


    @FXML
    private void initialize() {
        configurarColumnas();
        configurarSeleccionTabla();
        chkActivo.setSelected(true);
        tblCategorias.setItems(categorias);
        try {
            recargar();
        } catch (SQLException ex) {
            reportarErrorBD(ex, "cargar las categorías");
        }
    }

    private void recargar() throws SQLException {
        categorias.setAll(dao.listar());
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
    }

    private void configurarSeleccionTabla() {
        tblCategorias.getSelectionModel().selectedItemProperty().addListener(
                (obs, viejo, nuevo) -> {
                    if (nuevo != null) cargarEnFormulario(nuevo);
                });
    }

    private void cargarEnFormulario(Categoria c) {
        seleccionada = c;
        txtId.setText(String.valueOf(c.getId()));
        txtNombre.setText(c.getNombre());
        chkActivo.setSelected(c.isActivo());
    }


    @FXML
    private void guardar() {
        Categoria nueva = construirDesdeFormulario(null);
        if (!validarConConfirmacion(nueva)) return;
        ejecutarSeguro(() -> dao.guardar(nueva), "Categoría agregada correctamente.");
    }

    @FXML
    private void actualizar() {
        if (seleccionada == null) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING,
                    "Seleccione una categoría de la tabla primero.");
            return;
        }
        Categoria editada = construirDesdeFormulario(seleccionada.getId());
        if (!validarConConfirmacion(editada)) return;
        ejecutarSeguro(() -> dao.actualizar(editada), "Categoría actualizada correctamente.");
    }

    @FXML
    private void eliminar() {
        if (seleccionada == null) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING,
                    "Seleccione una categoría de la tabla primero.");
            return;
        }

        try {
            if (productoDAO.tieneProductos(seleccionada.getId())) {
                Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING,
                        "No se puede eliminar: hay productos asociados a esta categoría.\n"
                                + "Puede desactivarla en su lugar.");
                return;
            }
        } catch (SQLException ex) {
            reportarErrorBD(ex, "verificar los productos asociados");
            return;
        }

        if (!Mensajes.confirmar(txtNombre,
                "¿Eliminar la categoría '" + seleccionada.getNombre() + "'?")) {
            return;
        }

        int id = seleccionada.getId();
        ejecutarSeguro(() -> dao.eliminar(id), "Categoría eliminada correctamente.");
    }

    @FXML
    private void limpiar() {
        seleccionada = null;
        txtId.clear();
        txtNombre.clear();
        chkActivo.setSelected(true);
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }



    private Categoria construirDesdeFormulario(Integer id) {
        return new Categoria(
                id,
                txtNombre.getText() == null ? "" : txtNombre.getText().trim(),
                chkActivo.isSelected()
        );
    }


    private boolean validarConConfirmacion(Categoria categoria) {
        ResultadoValidacion res;
        try {
            res = validador.validar(categoria);
        } catch (SQLException ex) {
            reportarErrorBD(ex, "validar la categoría");
            return false;
        }

        if (!res.isValido()) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING, res.getMensaje());
            if (res.getCampo() == ResultadoValidacion.Campo.NOMBRE) {
                txtNombre.requestFocus();
            }
            return false;
        }
        if (res.getTipo() == ResultadoValidacion.Tipo.ADVERTENCIA) {
            return Mensajes.confirmar(txtNombre, res.getMensaje());
        }
        return true;
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
            Mensajes.mostrar(txtNombre, Alert.AlertType.ERROR,
                    "Operación fallida: " + ex.getMessage());
            return;
        }

        if (mensajeExito != null) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.INFORMATION, mensajeExito);
        }
        limpiar();
        try {
            recargar();
        } catch (SQLException ex) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING,
                    "La operación se realizó, pero no se pudo actualizar la tabla.");
            System.err.println("[BD] Error al recargar categorías: " + ex.getMessage());
        }
    }

    private void reportarErrorBD(SQLException ex, String contexto) {
        System.err.println("[BD] Error al " + contexto + ": " + ex.getMessage());
        Mensajes.mostrar(txtNombre, Alert.AlertType.ERROR,
                "No fue posible completar la operación.\n"
                        + "Verifique su conexión a la base de datos e intente nuevamente.");
    }
}