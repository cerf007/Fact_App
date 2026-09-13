package ni.edu.uam.fact_app.util;

import ni.edu.uam.fact_app.models.Producto;

import java.math.BigDecimal;
import java.util.List;

public class ProductoValidador implements Validador<Producto> {

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
        if (producto.getCategoria() == null) {
            return ResultadoValidacion.error("Debe seleccionar una categoría.");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoValidacion.error("El precio debe ser un número mayor a cero.");
        }
        if (producto.getExistencia() < 0) {
            return ResultadoValidacion.error("La existencia no puede ser negativa.");
        }

        String codigoNuevo = producto.getCodigo().trim();
        String nombreNuevo = producto.getNombre().trim().toLowerCase();
        List<Producto> productosExistentes = DataRepository.getProductos();

        for (Producto p : productosExistentes) {
            if (producto.getId() != null && producto.getId().equals(p.getId())) {
                continue;
            }

            if (p.getCodigo().equalsIgnoreCase(codigoNuevo)) {
                return ResultadoValidacion.error("El código '" + p.getCodigo() + "' ya está asignado a otro producto.");
            }

            if (p.getNombre().toLowerCase().equals(nombreNuevo)) {
                return ResultadoValidacion.error("Ya existe un producto con el nombre: '" + p.getNombre() + "'.");
            }
        }

        return ResultadoValidacion.exito();
    }
}