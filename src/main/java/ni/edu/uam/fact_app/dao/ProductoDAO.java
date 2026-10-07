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
    public void guardar(Producto entidad) throws SQLException {
        String sql = """
                INSERT INTO producto
                    (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setInt(3, entidad.getCategoria().getId());
            ps.setBigDecimal(4, entidad.getPrecioVenta());
            ps.setInt(5, entidad.getExistencia());
            ps.setString(6, entidad.getRutaImagen());
            ps.setBoolean(7, entidad.isActivo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) entidad.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public List<Producto> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY p.id";
        List<Producto> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    @Override
    public Producto buscar(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    @Override
    public void actualizar(Producto entidad) throws SQLException {
        String sql = """
                UPDATE producto
                   SET codigo = ?, nombre = ?, categoria_id = ?,
                       precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ?
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
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneProductos(int categoriaId) throws SQLException {
        String sql = "SELECT 1 FROM producto WHERE categoria_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        String sql = (idExcluir == null)
                ? "SELECT 1 FROM producto WHERE LOWER(codigo) = LOWER(?) LIMIT 1"
                : "SELECT 1 FROM producto WHERE LOWER(codigo) = LOWER(?) AND id <> ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            if (idExcluir != null) ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
        String sql = (idExcluir == null)
                ? "SELECT 1 FROM producto WHERE LOWER(nombre) = LOWER(?) LIMIT 1"
                : "SELECT 1 FROM producto WHERE LOWER(nombre) = LOWER(?) AND id <> ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (idExcluir != null) ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
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