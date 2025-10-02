package lmc.user.model;

public enum UserRole {

    USER("потребител"),
    ADMIN("администратор");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

