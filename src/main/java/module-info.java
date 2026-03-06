module school.coda.adam_lucie_verena.bataillejavale {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;

    opens school.coda.adam_lucie_venera.bataillejavale.ui to javafx.fxml;
    exports school.coda.adam_lucie_venera.bataillejavale.ui;
}