package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.SceneManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuPrincipalController {

    @FXML
    private Label lblTotalProductos;

    @FXML
    private Label lblTotalCategorias;

    @FXML
    private Label lblTotalPrecio;

    @FXML
    public void initialize() {
    }


    public void actualizarDashboard(List<Producto> listaProductos) {
        if (listaProductos == null || listaProductos.isEmpty()) {
            lblTotalProductos.setText("0");
            lblTotalCategorias.setText("0");
            lblTotalPrecio.setText("C$ 0.00");
            return;
        }

        int totalExistencia = listaProductos.stream()
                .mapToInt(Producto::getExistencia)
                .sum();

        long totalCategorias = listaProductos.stream()
                .filter(p -> p.getCategoria() != null)
                .map(Producto::getCategoria)
                .distinct()
                .count();

        BigDecimal valorTotal = listaProductos.stream()
                .filter(p -> p.getPrecioVenta() != null)
                .map(p -> p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getExistencia())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalProductos.setText(String.valueOf(totalExistencia));
        lblTotalCategorias.setText(String.valueOf(totalCategorias));

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "NI"));
        lblTotalPrecio.setText(formatoMoneda.format(valorTotal));
    }

    @FXML
    private void abrirProductos() {
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/producto-view.fxml",
                    "Gestión de productos");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "No fue posible abrir Productos.").showAndWait();
        }
    }

    @FXML
    private void salir() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea cerrar la aplicación?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
            Platform.exit();
    }
}