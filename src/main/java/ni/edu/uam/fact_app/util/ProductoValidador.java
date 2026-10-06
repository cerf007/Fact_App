package ni.edu.uam.fact_app.util;

import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.models.Producto;

import java.math.BigDecimal;

public class ProductoValidador implements Validador<Producto> {

    private final ProductoDAO dao;

    public ProductoValidador() {
        this(new ProductoDAO());
    }

    public ProductoValidador(ProductoDAO dao) {
        this.dao = dao;
    }

    @Override
    public ResultadoValidacion validar(Producto producto) {
        if (producto == null) {
            return ResultadoValidacion.error("El producto no puede ser nulo.");
        }
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            return ResultadoValidacion.error("El código del producto es obligatorio.");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            return ResultadoValidacion.error("El nombre del producto es obligatorio.");
        }
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            return ResultadoValidacion.error("Debe seleccionar una categoría.");
        }
        if (producto.getPrecioVenta() == null
                || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoValidacion.error("El precio debe ser un número mayor a cero.");
        }
        if (producto.getExistencia() < 0) {
            return ResultadoValidacion.error("La existencia no puede ser negativa.");
        }

        if (dao.existeCodigo(producto.getCodigo().trim(), producto.getId())) {
            return ResultadoValidacion.error(
                    "Ya existe un producto con el código '" + producto.getCodigo().trim() + "'.");
        }

        if (dao.existeNombre(producto.getNombre().trim(), producto.getId())) {
            return ResultadoValidacion.error(
                    "Ya existe un producto con el nombre '" + producto.getNombre().trim() + "'.");
        }

        return ResultadoValidacion.exito();
    }
}