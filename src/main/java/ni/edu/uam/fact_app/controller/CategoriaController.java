package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.util.CategoriaValidador;
import ni.edu.uam.fact_app.util.ResultadoValidacion;
import ni.edu.uam.fact_app.util.Validador;
import ni.edu.uam.fact_app.util.Mensajes;

public class CategoriaController {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtNombre;
    @FXML
    private CheckBox chkActivo;

    @FXML
    private TableView<Categoria> tblCategorias;
    @FXML
    private TableColumn<Categoria, Integer> colId;
    @FXML
    private TableColumn<Categoria, String> colNombre;
    @FXML
    private TableColumn<Categoria, Boolean> colActivo;

    private final CategoriaDAO dao = new CategoriaDAO();
    private final Validador<Categoria> validador = new CategoriaValidador(dao);

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        chkActivo.setSelected(true);
        recargar();
    }

    private void recargar() {
        tblCategorias.setItems(
                FXCollections.observableArrayList(dao.listar())
        );
    }

    @FXML
    private void guardar() {
        Categoria nueva = new Categoria(
                null,
                txtNombre.getText() == null ? "" : txtNombre.getText().trim(),
                chkActivo.isSelected()
        );

        ResultadoValidacion res = validador.validar(nueva);
        if (!res.isValido()) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.WARNING, res.getMensaje());
            return;
        }

        if (res.getTipo() == ResultadoValidacion.Tipo.ADVERTENCIA) {
            if (!Mensajes.confirmar(txtNombre, res.getMensaje())) {
                return;
            }
        }

        try {
            dao.guardar(nueva);
            Mensajes.mostrar(txtNombre, Alert.AlertType.INFORMATION, "Categoría agregada correctamente.");
            limpiar();
            recargar();
        } catch (RuntimeException ex) {
            Mensajes.mostrar(txtNombre, Alert.AlertType.ERROR, "No se pudo guardar: " + ex.getMessage());
        }
    }

        @FXML
        private void limpiar () {
            txtId.clear();
            txtNombre.clear();
            chkActivo.setSelected(true);
        }

        @FXML
        private void cerrar () {
            ((Stage) txtNombre.getScene().getWindow()).close();
        }

}