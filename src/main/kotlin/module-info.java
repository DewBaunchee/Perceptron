module by.varyvoda.matvey.perceptron {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires kotlin.stdlib.jdk8;

    opens by.varyvoda.matvey.perceptron to javafx.fxml;
    exports by.varyvoda.matvey.perceptron;
}