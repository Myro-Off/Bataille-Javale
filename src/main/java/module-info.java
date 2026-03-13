module school.coda.adam_lucie_verena.bataillejavale {
    requires javafx.controls;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires annotations;

    exports school.coda.adam_lucie_verena.bataillejavale;

    opens school.coda.adam_lucie_verena.bataillejavale to com.almasb.fxgl.all;

}