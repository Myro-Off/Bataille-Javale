module school.coda.adam_lucie_verena.bataillejavale {
    requires javafx.controls;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires annotations;

    exports school.coda.adam_lucie_verena.bataillejavale.core.model;
    exports school.coda.adam_lucie_verena.bataillejavale.view;
    exports school.coda.adam_lucie_verena.bataillejavale.core.engine;
    exports school.coda.adam_lucie_verena.bataillejavale.core.events;

    opens school.coda.adam_lucie_verena.bataillejavale.core.events to com.almasb.fxgl.all;
    exports school.coda.adam_lucie_verena.bataillejavale;
    exports school.coda.adam_lucie_verena.bataillejavale.view.vfx;
    exports school.coda.adam_lucie_verena.bataillejavale.view.scene;
    exports school.coda.adam_lucie_verena.bataillejavale.view.grid;
    exports school.coda.adam_lucie_verena.bataillejavale.view.component;
    exports school.coda.adam_lucie_verena.bataillejavale.controller;
}