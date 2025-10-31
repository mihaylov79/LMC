package lmc.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final  String ALL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890!@$#%^&*";

    public String generate(){

        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 15; i++) {
            int index = RANDOM.nextInt(ALL.length());
            sb.append((ALL.charAt(index)));
        }

        return sb.toString();
    }


}
