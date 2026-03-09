module school.coda.adam_lucie_verena.bataillejavale {
    requires javafx.controls;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires annotations;

    exports school.coda.adam_lucie_verena.bataillejavale.core.model;
    exports school.coda.adam_lucie_verena.bataillejavale.ui;
    exports school.coda.adam_lucie_verena.bataillejavale.core.engine;
    exports school.coda.adam_lucie_verena.bataillejavale.core.events;

    opens school.coda.adam_lucie_verena.bataillejavale.core.events to com.almasb.fxgl.all;
}