package ni.edu.uam.fact_app.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface Crud<T> {

    void guardar(T entidad) throws SQLException;

    List<T> listar() throws SQLException;

    T buscar(int id) throws SQLException;

    void actualizar(T entidad) throws SQLException;

    void eliminar(int id) throws SQLException;
}