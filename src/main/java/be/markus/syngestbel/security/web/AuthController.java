package be.markus.syngestbel.security.web;

import be.markus.syngestbel.security.auth.AuthRecord;
import be.markus.syngestbel.security.auth.AuthService;
import be.markus.syngestbel.security.entity.Group;
import be.markus.syngestbel.security.entity.User;
import be.markus.syngestbel.security.requestModel.UserRegistrationRequest;
import be.markus.syngestbel.security.response.AuthResponse;
import be.markus.syngestbel.security.service.group.GroupServiceImpl;
import be.markus.syngestbel.security.service.user.UserServiceImpl;
import be.markus.syngestbel.security.tool.Tools;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// must change in the log subject by user id

@RestController
@RequestMapping(path="/api/v1/auth")

@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserServiceImpl userService;

    private final GroupServiceImpl groupService;

    private PasswordEncoder passwordEncoder;


    @Autowired
    public AuthController(
            AuthService authService,
            UserServiceImpl userService,
            GroupServiceImpl groupService,
            PasswordEncoder passwordEncoder){
        this.authService = authService;
        this.userService = userService;
        this.groupService = groupService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> jwtToken(
            AuthRecord authRecord
    ) {

        Map<String,Object>  response = new HashMap<>();
        response.put("time", Instant.now());
        Map<String,Object>  request;



        try{
            request = this.authService.authentification(authRecord);
            response.putAll(request);
            return new ResponseEntity<>(Map.of("datas",response),HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(Map.of("errorMessage",e.getMessage()),HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse>
    login(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password
    ) throws RuntimeException{

        if(username.trim().isEmpty() || password.trim().isEmpty()){
           return builderResponse("Login or Password can't be empty ! ",null,null,HttpStatus.BAD_REQUEST);
        }

        UserDetails u = this.userService.loadUserByUsernameAndPassword(username,password);

        if(u==null){
            return builderResponse("User was not found",null,null,HttpStatus.NOT_FOUND);
        }

        return builderResponse("User found !!",null,u,HttpStatus.OK);
    }


    @PostMapping(path="/register",consumes = "application/json")
    @ResponseBody
    public ResponseEntity<AuthResponse> registeredNewUser(@RequestBody UserRegistrationRequest userRequest)
            throws RuntimeException{

        String username = userRequest.getUsername();
        String email = userRequest.getEmail();
        String password = userRequest.getPassword();
        Group group = new Group();

        // verify if we have all parameters
        if(username==null || email==null || password==null){
            return builderResponse("Parameter required !",null,null,HttpStatus.BAD_REQUEST);
        }

        // create new identification user
        StringBuilder userID = new StringBuilder();
        do {
            userID.append(userRequest.getGroup(), 0, 3);
            userID.append(new Tools().randomHexadecimal(6));
        } while (checkId(userID.toString()));

        // check if the email exist yet into user table
        if(!checkEmailExist(email)){
            return builderResponse("User exist with this email. Please sign in ! ",null,null,HttpStatus.CONFLICT);
        }

        group = checkGroup(userRequest.getGroup());
        // check if assigned group exist
        if(group==null){
            return this.builderResponse(null,"This group doesn't exist !",null,HttpStatus.BAD_REQUEST);
        }

        // try to save the new user
        try{
            User u = User.builder().build();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(password));
            u.setGroup(group);
            u.setUserIdentification(userID.toString());
            this.userService.save(u);
            return builderResponse("User registered with success ! This user can sign in now !!",null,null,HttpStatus.OK);

        }catch(Exception e){
            return builderResponse(null,e.getMessage(),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Group checkGroup(String name) {
        return this.groupService.findByName(name).orElse(null);
    }

    private boolean checkEmailExist(String email) {
        return this.userService.loadUserByUsername(email)==null;
    }

    private boolean checkId(String ident) {
        return this.userService.findUserIdentification(ident);
    }


    private ResponseEntity<AuthResponse> builderResponse(String message,String devMessage, Object datas, HttpStatus httpStatus) {
        if(datas==null) datas="";
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage(message);
        authResponse.setData(datas);
        authResponse.setDevMessage(devMessage);
        return ResponseEntity.status(httpStatus).body(authResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<AuthResponse> handleMissingParamaterException(MissingServletRequestParameterException ex){
        return builderResponse(" Login and Password are required ! ",null,null,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<AuthResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        StackTraceElement[] stackTrace = ex.getStackTrace();
        return builderResponse(null,ex.getMessage()+" ||| "+stackTrace[0].toString(),null,HttpStatus.BAD_REQUEST);
    }





}
