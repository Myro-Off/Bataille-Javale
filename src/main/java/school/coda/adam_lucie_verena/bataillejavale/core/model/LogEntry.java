package school.coda.adam_lucie_verena.bataillejavale.core.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LogEntry {

    private final ObjectProperty<LocalDateTime> time;
    private final StringProperty name;
    private final StringProperty action;
    private final ObjectProperty<LogType> type;


    public LogEntry(LocalTime now, String name, String action, LogType type) {
        this.name = new SimpleStringProperty(name);
        this.action = new SimpleStringProperty(action);
        this.time = new SimpleObjectProperty<>(LocalDateTime.now());
        this.type = new SimpleObjectProperty<>(type);
    }

    public LocalDateTime getTime() {
        return time.get();
    }
    public  String getName() {
        return name.get();
    }
    public  String getAction() {
        return action.get();
    }
    public LogType getType() {
        return type.get();
    }
}

