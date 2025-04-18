module com.bozzat.esepkersoft {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.slf4j;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires java.sql;
    requires jdk.incubator.vector;
    
    opens com.bozzat.esepkersoft to javafx.fxml;
    exports com.bozzat.esepkersoft;

    exports com.bozzat.esepkersoft.Controllers;
    opens com.bozzat.esepkersoft.Controllers to javafx.fxml;

    // ✅ Allow JavaFX to access properties in Models class
    opens com.bozzat.esepkersoft.Models to javafx.base;
    exports com.bozzat.esepkersoft.Interfaces;
    opens com.bozzat.esepkersoft.Interfaces to javafx.fxml;
}
