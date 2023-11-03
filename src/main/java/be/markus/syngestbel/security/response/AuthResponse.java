package be.markus.syngestbel.security.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
public class AuthResponse {


    private LocalDateTime sendAt;

    private String message;

    private  String devMessage;

    private Object data;

    public AuthResponse() {
        this.sendAt = LocalDateTime.now();
    }
}
