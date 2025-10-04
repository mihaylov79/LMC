package lmc.option.model;

public enum SupportedBy {
   ALL("-"),
    HERMA500("Herma 500");

   private final String description;

    SupportedBy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
