package ni.edu.uam.fact_app.util;

import java.sql.SQLException;

public interface Validador<T> {
    ResultadoValidacion validar(T objeto) throws SQLException;
}