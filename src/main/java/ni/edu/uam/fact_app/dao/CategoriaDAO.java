package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.DatabaseConnection;
import ni.edu.uam.fact_app.interfaces.Crud;
import ni.edu.uam.fact_app.models.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements Crud<Categoria> {

    @Override
    public void guardar(Categoria entidad) {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entidad.getNombre());
            ps.setBoolean(2, entidad.isActivo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Categoria> listar() {
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id";
        List<Categoria> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Categoria buscar(int id) {
        String sql = "SELECT id, nombre, activa FROM categoria WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void actualizar(Categoria entidad) {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setBoolean(2, entidad.isActivo());
            ps.setInt(3, entidad.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoría: " + e.getMessage(), e);
        }
    }


    public boolean existeNombre(String nombre, Integer idExcluir) {
        String sql = (idExcluir == null)
                ? "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?) LIMIT 1"
                : "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?) AND id <> ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            if (idExcluir != null) ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar nombre duplicado: " + e.getMessage(), e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getBoolean("activa")
        );
    }
}