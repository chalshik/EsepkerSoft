module com.bozzat.esepkersoft {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires eu.hansolo.tilesfx;

    opens com.bozzat.esepkersoft to javafx.fxml;
    exports com.bozzat.esepkersoft;
}