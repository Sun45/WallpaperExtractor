module cn.sun45_.wallpaperextractor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires java.prefs;
    requires java.desktop;
    requires com.dlsc.gemsfx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.logging;

    opens cn.sun45_.wallpaperextractor to javafx.fxml;
    exports cn.sun45_.wallpaperextractor;
    exports cn.sun45_.wallpaperextractor.controller;
    opens cn.sun45_.wallpaperextractor.controller to javafx.fxml;
}