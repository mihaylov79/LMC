package lmc.exceptions;

public class CurrencyFixingCanNotBeCreated extends IllegalArgumentException {

    public static final String DEFAULT_MESSAGE = "Фиксингът не може да бъде съзаден";

    public CurrencyFixingCanNotBeCreated() {
        super(DEFAULT_MESSAGE);
    }

    public CurrencyFixingCanNotBeCreated(String message){
        super(message);
    }
}
