package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.DataRepository;
import ni.edu.uam.fact_app.util.SceneManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuPrincipalController {

    @FXML private Label lblTotalProductos;
    @FXML private Label lblProductosActivos;
    @FXML private Label lblTotalCategorias;
    @FXML private Label lblTotalPrecio;

    @FXML
    public void initialize() {
        actualizarDashboard();
    }

    public void actualizarDashboard() {
        List<Producto> productos = DataRepository.getProductos();
        List<Categoria> categorias = DataRepository.getCategorias();

        int totalUnidades = productos.stream()
                .mapToInt(Producto::getExistencia)
                .sum();

        long productosDistintosActivos = productos.stream()
                .filter(Producto::isActivo)
                .count();

        long categoriasActivas = categorias.stream()
                .filter(Categoria::isActivo)
                .count();

        BigDecimal valorTotal = productos.stream()
                .filter(p -> p.getPrecioVenta() != null)
                .map(p -> p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getExistencia())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalProductos.setText(String.valueOf(totalUnidades));
        lblProductosActivos.setText(String.valueOf(productosDistintosActivos));
        lblTotalCategorias.setText(String.valueOf(categoriasActivas));

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "NI"));
        lblTotalPrecio.setText(formatoMoneda.format(valorTotal));
    }

    @FXML
    private void abrirCategorias() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/categoria-view.fxml", "Gestión de categorías");
            actualizarDashboard();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No fue posible abrir Categorías.").showAndWait();
        }
    }

    @FXML
    private void abrirProductos() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/producto-view.fxml", "Gestión de productos");
            actualizarDashboard();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No fue posible abrir Productos.").showAndWait();
        }
    }

    @FXML
    private void salir() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea cerrar la aplicación?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }
}