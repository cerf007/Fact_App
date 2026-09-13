package ni.edu.uam.fact_app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.util.DataRepository;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActivo;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActivo;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        tblCategorias.setItems(DataRepository.getCategorias());
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre de la categoría.");
            return;
        }

        int nuevoId = DataRepository.getCategorias().size() + 1;
        Categoria nuevaCategoria = new Categoria(
                nuevoId,
                txtNombre.getText().trim(),
                chkActivo.isSelected()
        );

        DataRepository.getCategorias().add(nuevaCategoria);
        mensaje(Alert.AlertType.INFORMATION, "Categoría agregada correctamente.");
        limpiar();
    }

    @FXML
    private void limpiar() {
        txtId.clear();
        txtNombre.clear();
        chkActivo.setSelected(true);
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}