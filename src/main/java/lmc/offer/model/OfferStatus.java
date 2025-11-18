package lmc.offer.model;

public enum OfferStatus {
    PENDING("На изчакване") ,
    ACCEPTED("Спечелена") ,
    REJECTED("Загубена"),
    CANCELED("Анулирана");

    private final String description;

    OfferStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
