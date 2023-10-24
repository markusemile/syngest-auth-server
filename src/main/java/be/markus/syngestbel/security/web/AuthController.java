package be.markus.syngestbel.security.web;

import be.markus.syngestbel.security.auth.AuthRecord;
import be.markus.syngestbel.security.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// must change in the log subject by user id

@RestController
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> jwtToken(
            AuthRecord authRecord
    ) {

        Map<String,Object>  response = new HashMap<>();
        response.put("time", Instant.now());
        Map<String,Object>  request = new HashMap<>();

        try{
            request = this.authService.authentification(authRecord);
            response.putAll(request);
            return new ResponseEntity<>(Map.of("datas",response),HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(Map.of("errorMessage",e.getMessage()),HttpStatus.BAD_REQUEST);
        }

    }


}
