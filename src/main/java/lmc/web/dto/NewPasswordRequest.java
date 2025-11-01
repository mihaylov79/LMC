package lmc.web.dto;

import lombok.Data;

@Data
public class NewPasswordRequest {

    private String password;

    private String confirmPassword;
}
