package ni.edu.uam.fact_app.util;

public class ResultadoValidacion {
    public enum Tipo { EXITO, ADVERTENCIA, ERROR }

    private final boolean valido;
    private final String mensaje;
    private final Tipo tipo;

    public ResultadoValidacion(boolean valido, String mensaje, Tipo tipo) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.tipo = tipo;
    }

    public static ResultadoValidacion exito() {
        return new ResultadoValidacion(true, "", Tipo.EXITO);
    }

    public static ResultadoValidacion error(String mensaje) {
        return new ResultadoValidacion(false, mensaje, Tipo.ERROR);
    }

    public static ResultadoValidacion advertencia(String mensaje) {
        return new ResultadoValidacion(true, mensaje, Tipo.ADVERTENCIA);
    }

    public boolean isValido() { return valido; }
    public String getMensaje() { return mensaje; }
    public Tipo getTipo() { return tipo; }
}