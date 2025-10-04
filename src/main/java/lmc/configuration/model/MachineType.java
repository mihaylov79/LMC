package lmc.configuration.model;

public enum MachineType {

    TXP("TxP"), TBXX("TBxx"), W1X0("W1x0"), VFFS500("VFFS500");

    private final String description;

    MachineType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
