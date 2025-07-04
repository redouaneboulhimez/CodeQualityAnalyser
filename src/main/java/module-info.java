module com.example.anayseurcodeai {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // JavaParser modules
    requires com.github.javaparser.core;
    requires com.github.javaparser.symbolsolver.core;

    opens com.example.anayseurcodeai to javafx.fxml;
    exports com.example.anayseurcodeai;
    exports com.example.anayseurcodeai.analyzer;
}
