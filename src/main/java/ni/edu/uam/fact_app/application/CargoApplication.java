package ni.edu.uam.fact_app.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CargoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Se añade '/fxml/' a la ruta del recurso
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ni/edu/uam/fact_app/fxml/cargo-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Gestión de Cargos");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}