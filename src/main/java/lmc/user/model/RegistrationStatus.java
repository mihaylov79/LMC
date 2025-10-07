package lmc.user.model;

public enum RegistrationStatus {
    NOT_REGISTERED("Не регистриран"),
    PENDING("На изчакване"),
    REGISTERED("Регистриран");

    private final String description;

    RegistrationStatus(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
