package ni.edu.uam.fact_app.util;

public class ResultadoValidacion {
    public enum Tipo { EXITO, ADVERTENCIA, ERROR }

    public enum Campo { NINGUNO, CODIGO, NOMBRE, CATEGORIA, PRECIO, EXISTENCIA }

    private final boolean valido;
    private final String mensaje;
    private final Tipo tipo;
    private final Campo campo;

    public ResultadoValidacion(boolean valido, String mensaje, Tipo tipo, Campo campo) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.campo = campo;
    }

    public static ResultadoValidacion exito() {
        return new ResultadoValidacion(true, "", Tipo.EXITO, Campo.NINGUNO);
    }

    public static ResultadoValidacion error(String mensaje) {
        return error(mensaje, Campo.NINGUNO);
    }

    public static ResultadoValidacion error(String mensaje, Campo campo) {
        return new ResultadoValidacion(false, mensaje, Tipo.ERROR, campo);
    }

    public static ResultadoValidacion advertencia(String mensaje) {
        return advertencia(mensaje, Campo.NINGUNO);
    }

    public static ResultadoValidacion advertencia(String mensaje, Campo campo) {
        return new ResultadoValidacion(true, mensaje, Tipo.ADVERTENCIA, campo);
    }

    public boolean isValido() { return valido; }
    public String getMensaje() { return mensaje; }
    public Tipo getTipo() { return tipo; }
    public Campo getCampo() { return campo; }
}