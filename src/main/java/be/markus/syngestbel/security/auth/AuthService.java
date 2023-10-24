package be.markus.syngestbel.security.auth;
;
import be.markus.syngestbel.security.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final String racine;
    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");

    @Autowired
    public AuthService(JwtService jwtService,
                       JwtDecoder jwtDecoder,
                       JwtEncoder jwtEncoder,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.racine = getClass().getSimpleName();
    }

    public Map<String,Object> authentification(AuthRecord authRecord){

        Map<String,Object> response = new HashMap<>();

        Map<String,String> matchAuth= new HashMap<>();
        Map<String,String> tokens = new HashMap<>();

        if(authRecord.grantType().equals("password")){
            try {
                matchAuth = getSubjectAndScope(authRecord.username(), authRecord.password());
                response.putAll(matchAuth);
                System.out.println(matchAuth.toString());

            } catch (Exception e) {
                throw  new RuntimeException(e.getMessage());
            }
        } else if (authRecord.grantType().equals("refreshToken")) {
            try {
                matchAuth = getSubjectAndScope(authRecord.refreshToken());
            } catch (Exception e) {
                throw  new RuntimeException(e.getMessage());
            }
        }else{
            throw new RuntimeException("You must to define grantType");
        }

        if(matchAuth!= null && matchAuth.size()==2){
            try {
                tokens = this.getTokens(matchAuth,authRecord.withRefreshToken());
                response.putAll(tokens);
                return response;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }else{
            throw new RuntimeException("Can't find subject and/or scope of user");
        }
    }


    public Map<String, String> getSubjectAndScope(String username, String password) {
        Map<String,String> result = new HashMap<>();
        Authentication auth=null;
        String subject = null;
        String scope = null;

        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
        } catch (AuthenticationException e) {
            throw new RuntimeException("User was not found");
        }
        try{
            subject = auth.getName();
            result.put("subject",subject);
        }catch(Exception e){
            throw new RuntimeException("Not possible to find the name of user");
        }
        try{
            scope=auth.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
            result.put("scope",scope);
        }catch(Exception e){
            throw new RuntimeException("Authorities was not found");
        }
        return result;
    }


    public Map<String, String> getSubjectAndScope(String token){
        Map<String,String> result = new HashMap<>();
        String subject = null;
        String scope = null;

        if (token == null)
            throw new RuntimeException("Refresh Token is required");

        Jwt decodeJwt = null;

        try {
            decodeJwt = jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new RuntimeException(e.getMessage());
        }

        try {
            subject=decodeJwt.getSubject();

        } catch (Exception e) {
            throw new RuntimeException("Error : can't extract subject of token");
        }

            UserDetails u = null;
        try {
            u = userDetailsService.loadUserByUsername(subject);
            result.put("username",u.getUsername());
            result.put("authorities",u.getAuthorities().stream().map(au->au.getAuthority()).collect(Collectors.joining("")));
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Error : can't find user :"+subject);
        }

        Collection<? extends GrantedAuthority> authorities = null;
        try {
            authorities = u.getAuthorities();
        } catch (Exception e) {
            throw new RuntimeException("Error : Cant' find authorities of user "+subject);
        }

        scope=authorities
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        if(subject!=null){
            result.put("subject",subject);
            result.put("scope",scope);
            return result;
        }else{
            throw new RuntimeException("Something wrong with token extractor");
        }
    }

    public Map<String, String> getTokens(Map<String, String> userDatas, boolean withRefreshToken) {

        Map<String,String> result = new HashMap<>();

        String subject = userDatas.get("subject");
        String scope = userDatas.get("scope");
        int expireToken = withRefreshToken ? 5 : 30;
        int expireRefresh = 1440;

        if(subject!=null && scope!= null) {
            String token = null;
            try {
                token = makeToken(subject, scope,expireToken);
                result.put("token",token);
                if(withRefreshToken){
                    String refreshToken=null;
                    try {
                        refreshToken=makeToken(subject,scope,expireRefresh);
                        result.put("refreshToken",refreshToken);
                    } catch (JwtException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }catch(JwtException e){
                throw new RuntimeException(e.getMessage());
            }
        }
        return result;
    }

    public String makeToken(String subject,String scope,int expire){
        JwtClaimsSet jwtClaimsSet = null;
        Instant instant = Instant.now();
        String jwtAccessToken = null;

        if(subject!=null){
            jwtClaimsSet = JwtClaimsSet.builder()
                    .subject(subject)
                    .issuedAt(instant)
                    .expiresAt(instant.plus( expire, ChronoUnit.MINUTES))
                    .issuer("auth-service")
                    .claim("scope", scope)
                    .build();
        }

        try {
            jwtAccessToken = this.jwtService.jwtEncoder().encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        } catch (JwtEncodingException e) {
            throw new RuntimeException("Error : creation token unsuccessfully ! ");
        }

         return jwtAccessToken;
    }
}
