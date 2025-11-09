package lmc.exceptions;

public class CompanyAlreadyExistException extends RuntimeException{

    private static final String DEFAULT_MESSAGE = "Тази компания вече съществува в базата данни!";

   public CompanyAlreadyExistException(){
       super(DEFAULT_MESSAGE);
   }

   public CompanyAlreadyExistException(String message){
       super(message);
   }
}
