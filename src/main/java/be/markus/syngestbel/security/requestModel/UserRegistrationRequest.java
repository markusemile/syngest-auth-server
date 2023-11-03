package be.markus.syngestbel.security.requestModel;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Builder
@Data
public class UserRegistrationRequest {

    private String username;

    private String email;

    private String password;

    private String group;

    public UserRegistrationRequest(String username, String email, String password, String group) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.group = (group!=null) ? group : "USER";
    }
}
