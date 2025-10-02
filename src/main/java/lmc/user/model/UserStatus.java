package lmc.user.model;

public enum UserStatus {

    ACTIVE("активен"),
    INACTIVE("деактивиран");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
