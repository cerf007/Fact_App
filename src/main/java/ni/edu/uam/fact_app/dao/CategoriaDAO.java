package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.DatabaseConnection;
import ni.edu.uam.fact_app.interfaces.Crud;
import ni.edu.uam.fact_app.models.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements Crud<Categoria> {

    @Override
    public void guardar(Categoria entidad) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getNombre());
            ps.setBoolean(2, entidad.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) entidad.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public List<Categoria> listar() throws SQLException {
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id";
        List<Categoria> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    @Override
    public Categoria buscar(int id) throws SQLException {
        String sql = "SELECT id, nombre, activa FROM categoria WHERE id = ?";
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
    public void actualizar(Categoria entidad) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setBoolean(2, entidad.isActivo());
            ps.setInt(3, entidad.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
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