package school.coda.adam_lucie_verena.bataillejavale.core.achievement;

public enum AchievementType {

    FIRST_WIN ("Série de victoires !", "Incroyable ! Vous avez enchaîné 10 victoires consécutives !"),
    FAST_WIN("Série de victoires !", "Incroyable ! Vous avez enchaîné 50 victoires consécutives !"),
    VETERAN_10("Série de victoires !", "Incroyable ! Vous avez enchaîné 100 victoires consécutives !");

    private final String name;
    private final String description;

    AchievementType(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        System.out.println("je return le name");
        return name;
    }

    public String getDescription() {
        return description;
    }
}