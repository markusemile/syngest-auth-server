package be.markus.syngestbel.security.web;

import jakarta.websocket.server.PathParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class testRestAPI {

    @GetMapping("/dataTest")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER','SCOPE_ADMIN')")
    public Map<String,Object> dataTest(Authentication authentication){

        return Map.of(
                "message","Data test",
                "username",authentication.getName(),
                "role",authentication.getAuthorities()
                );
    }

    @PostMapping("/saveData")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @ResponseBody
    public Map<String,Object> saveData(
            Authentication authentication,
            @RequestParam(name="data") String data){

       Map<String,Object> mapp = new HashMap<>();
       mapp.put("message","Sata saved !");
       mapp.put("data",data);
       return mapp;
    }

}
