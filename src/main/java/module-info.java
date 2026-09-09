module ni.edu.uam.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    exports ni.edu.uam.fact_app.application;
    opens ni.edu.uam.fact_app.application to javafx.fxml;
    exports ni.edu.uam.fact_app.controller;
    opens ni.edu.uam.fact_app.controller to javafx.fxml;
    exports ni.edu.uam.fact_app;
    opens ni.edu.uam.fact_app to javafx.fxml;
}