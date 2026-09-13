package ni.edu.uam.fact_app.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;

public class DataRepository {

    private static final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private static final ObservableList<Producto> productos = FXCollections.observableArrayList();

    static {
        // Carga inicial de categorías por defecto
        categorias.add(new Categoria(1, "Alimentos", true));
        categorias.add(new Categoria(2, "Bebidas", true));
        categorias.add(new Categoria(3, "Limpieza", true));
    }

    public static ObservableList<Categoria> getCategorias() {
        return categorias;
    }

    public static ObservableList<Producto> getProductos() {
        return productos;
    }
}