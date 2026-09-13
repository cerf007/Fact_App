package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.DataRepository;
import ni.edu.uam.fact_app.util.ProductoValidador;
import ni.edu.uam.fact_app.util.ResultadoValidacion;
import ni.edu.uam.fact_app.util.Validador;

import java.io.File;
import java.math.BigDecimal;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, String> colActivo;

    private final Validador<Producto> validador = new ProductoValidador();
    private String rutaImagen;

    @FXML
    private void initialize() {
        cmbCategoria.setItems(DataRepository.getCategorias());

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoria() != null ?
                        cellData.getValue().getCategoria().getNombre() : "")
        );

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));

        colActivo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActivo() ? "Sí" : "No")
        );

        tblProductos.setItems(DataRepository.getProductos());
        chkActivo.setSelected(true);
    }

    @FXML
    private void guardar() {
        try {
            BigDecimal precio = txtPrecio.getText().isBlank() ? BigDecimal.ZERO : new BigDecimal(txtPrecio.getText().trim());
            int existencia = txtExistencia.getText().isBlank() ? -1 : Integer.parseInt(txtExistencia.getText().trim());

            Producto nuevoProducto = new Producto(
                    null,
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected()
            );

            // Delegar la validación al ProductoValidador
            ResultadoValidacion res = validador.validar(nuevoProducto);

            if (!res.isValido()) {
                mensaje(Alert.AlertType.WARNING, res.getMensaje());
                return;
            }

            DataRepository.getProductos().add(nuevoProducto);
            mensaje(Alert.AlertType.INFORMATION, "Producto agregado correctamente.");
            limpiar();

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia deben ser valores numéricos válidos.");
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagen = null;
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}