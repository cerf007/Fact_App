package ni.edu.uam.fact_app.interfaces;

import java.util.List;

public interface Crud<T> {

    void guardar(T entidad);

    List<T> listar();

    T buscar(int id);

    void actualizar(T entidad);

    void eliminar(int id);
}