package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.DatabaseConnection;
import ni.edu.uam.fact_app.interfaces.Crud;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO implements Crud<Producto> {

    private static final String SELECT_BASE = """
            SELECT  p.id,
                    p.codigo,
                    p.nombre,
                    p.precio_venta,
                    p.existencia,
                    p.ruta_imagen,
                    p.activo,
                    c.id        AS cat_id,
                    c.nombre    AS cat_nombre,
                    c.activa    AS cat_activa
            FROM producto p
            INNER JOIN categoria c ON c.id = p.categoria_id
            """;

    @Override
    public void guardar(Producto entidad) {
        String sql = """
                INSERT INTO producto
                    (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setInt(3, entidad.getCategoria().getId()); // ← solo el id
            ps.setBigDecimal(4, entidad.getPrecioVenta());
            ps.setInt(5, entidad.getExistencia());
            ps.setString(6, entidad.getRutaImagen());
            ps.setBoolean(7, entidad.isActivo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) entidad.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> listar() {
        String sql = SELECT_BASE + " ORDER BY p.id";
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Producto buscar(int id) {
        String sql = SELECT_BASE + " WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void actualizar(Producto entidad) {
        String sql = """
                UPDATE producto
                   SET codigo = ?,
                       nombre = ?,
                       categoria_id = ?,
                       precio_venta = ?,
                       existencia = ?,
                       ruta_imagen = ?,
                       activo = ?
                 WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setInt(3, entidad.getCategoria().getId());
            ps.setBigDecimal(4, entidad.getPrecioVenta());
            ps.setInt(5, entidad.getExistencia());
            ps.setString(6, entidad.getRutaImagen());
            ps.setBoolean(7, entidad.isActivo());
            ps.setInt(8, entidad.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getInt("cat_id"),
                rs.getString("cat_nombre"),
                rs.getBoolean("cat_activa")
        );

        return new Producto(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                categoria,
                rs.getBigDecimal("precio_venta"),
                rs.getInt("existencia"),
                rs.getString("ruta_imagen"),
                rs.getBoolean("activo")
        );
    }
}