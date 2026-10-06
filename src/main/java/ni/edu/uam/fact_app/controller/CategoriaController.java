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
        recargar();
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

    private void recargar() {
        categorias.setAll(dao.listar());
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
        if (!Mensajes.confirmar(txtNombre,
                "¿Eliminar la categoría '" + seleccionada.getNombre() + "'?")) {
            return;
        }

        try {
            if (productoDAO.tieneProductos(seleccionada.getId())) {
                Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING,
                        "No se puede eliminar: hay productos asociados a esta categoría.\n"
                                + "Puede desactivarla en su lugar.");
                return;
            }

            dao.eliminar(seleccionada.getId());
            Mensajes.mostrar(txtNombre, Alert.AlertType.INFORMATION,
                    "Categoría eliminada correctamente.");
            limpiar();
            recargar();
        } catch (RuntimeException ex) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.ERROR,
                    "Operación fallida: " + ex.getMessage());
        }
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
        ResultadoValidacion res = validador.validar(categoria);
        if (!res.isValido()) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING, res.getMensaje());
            return false;
        }
        if (res.getTipo() == ResultadoValidacion.Tipo.ADVERTENCIA) {
            return Mensajes.confirmar(txtNombre, res.getMensaje());
        }
        return true;
    }

    private void ejecutarSeguro(Runnable accion, String mensajeExito) {
        try {
            accion.run();
            if (mensajeExito != null) {
                Mensajes.mostrar(txtNombre, Alert.AlertType.INFORMATION, mensajeExito);
            }
            limpiar();
            recargar();
        } catch (RuntimeException ex) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.ERROR,
                    "Operación fallida: " + ex.getMessage());
        }
    }
}