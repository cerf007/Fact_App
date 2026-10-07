package ni.edu.uam.fact_app.util;

import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.models.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductoValidador implements Validador<Producto> {

    private final ProductoDAO dao;

    public ProductoValidador() { this(new ProductoDAO()); }
    public ProductoValidador(ProductoDAO dao) { this.dao = dao; }

    @Override
    public ResultadoValidacion validar(Producto producto) throws SQLException {
        if (producto == null) {
            return ResultadoValidacion.error("El producto no puede ser nulo.");
        }
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            return ResultadoValidacion.error(
                    "El código del producto es obligatorio.", ResultadoValidacion.Campo.CODIGO);
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            return ResultadoValidacion.error(
                    "El nombre del producto es obligatorio.", ResultadoValidacion.Campo.NOMBRE);
        }
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            return ResultadoValidacion.error(
                    "Debe seleccionar una categoría.", ResultadoValidacion.Campo.CATEGORIA);
        }
        if (producto.getPrecioVenta() == null
                || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoValidacion.error(
                    "El precio debe ser un número mayor a cero.", ResultadoValidacion.Campo.PRECIO);
        }
        if (producto.getExistencia() < 0) {
            return ResultadoValidacion.error(
                    "La existencia no puede ser negativa.", ResultadoValidacion.Campo.EXISTENCIA);
        }

        if (dao.existeCodigo(producto.getCodigo().trim(), producto.getId())) {
            return ResultadoValidacion.error(
                    "Ya existe un producto con el código '" + producto.getCodigo().trim() + "'.",
                    ResultadoValidacion.Campo.CODIGO);
        }
        if (dao.existeNombre(producto.getNombre().trim(), producto.getId())) {
            return ResultadoValidacion.error(
                    "Ya existe un producto con el nombre '" + producto.getNombre().trim() + "'.",
                    ResultadoValidacion.Campo.NOMBRE);
        }
        return ResultadoValidacion.exito();
    }
}