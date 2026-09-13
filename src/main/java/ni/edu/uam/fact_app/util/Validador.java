package ni.edu.uam.fact_app.util;

public interface Validador<T> {
    ResultadoValidacion validar(T objeto);
}