package ni.edu.uam.fact_app.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Mensajes {

    private Mensajes() {
    }

    public static void mostrar(Node origen, Alert.AlertType tipo, String texto) {
        crear(origen, tipo, texto, ButtonType.OK).showAndWait();
    }

    public static boolean confirmar(Node origen, String pregunta) {
        return crear(origen, Alert.AlertType.CONFIRMATION, pregunta,
                ButtonType.OK, ButtonType.CANCEL)
                .showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static Alert crear(Node origen, Alert.AlertType tipo, String texto, ButtonType... botones) {
        Alert alerta = new Alert(tipo, texto, botones);
        alerta.setTitle(titulo(tipo));
        alerta.setHeaderText(null);
        if (origen != null && origen.getScene() != null) {
            alerta.initOwner(origen.getScene().getWindow());
        }
        return alerta;
    }

    private static String titulo(Alert.AlertType tipo) {
        return switch (tipo) {
            case WARNING      -> "Advertencia";
            case ERROR        -> "Error";
            case CONFIRMATION -> "Confirmación";
            default           -> "Información";
        };
    }
}