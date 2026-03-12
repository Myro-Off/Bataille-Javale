package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GameLog {

    private final List<LogEntry> entries = new ArrayList<>();

    public void addEntry(String name, String action, LogType type) {
        // On crée le record avec l'heure actuelle
      entries.add(new LogEntry(LocalTime.now(), name, action, type));
    }

    public List<LogEntry> getEntries() {
        return entries;
    }
}
