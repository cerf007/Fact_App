package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.Mensajes;
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

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    public void initialize() {
        actualizarDashboard();
    }

    public void actualizarDashboard() {
        List<Producto> productos;
        List<Categoria> categorias;
        try {
            productos = productoDAO.listar();
            categorias = categoriaDAO.listar();
        } catch (RuntimeException ex) {
            productos = List.of();
            categorias = List.of();
            Mensajes.mostrar(lblTotalProductos, Alert.AlertType.WARNING,
                    "No se pudo consultar la base de datos: " + ex.getMessage());
        }

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

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.of("es", "NI"));
        lblTotalPrecio.setText(formatoMoneda.format(valorTotal));
    }

    @FXML
    private void abrirCategorias() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/categoria-view.fxml",
                    "Gestión de categorías");
            actualizarDashboard();
        } catch (IOException e) {
            Mensajes.mostrar(lblTotalProductos, Alert.AlertType.ERROR,
                    "No fue posible abrir Categorías.");
        }
    }

    @FXML
    private void abrirProductos() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/producto-view.fxml",
                    "Gestión de productos");
            actualizarDashboard();
        } catch (IOException e) {
            Mensajes.mostrar(lblTotalProductos, Alert.AlertType.ERROR,
                    "No fue posible abrir Categorías.");
        }
    }

    @FXML
    private void salir() {
        if (Mensajes.confirmar(lblTotalProductos, "¿Desea cerrar la aplicación?")) {
            Platform.exit();
        }
    }
}