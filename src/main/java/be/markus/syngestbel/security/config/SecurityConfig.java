package be.markus.syngestbel.security.config;

import be.markus.syngestbel.security.service.user.UserService;
import be.markus.syngestbel.security.service.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

   private final PasswordEncoder passwordEncoder;
    private final UserServiceImpl userService;

    @Autowired
    public SecurityConfig(PasswordEncoder passwordEncoder,UserServiceImpl userService) {
        this.passwordEncoder=passwordEncoder;
        this.userService=userService;
    }


    protected  void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userService);
    }




    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

       return http
                .csrf(AbstractHttpConfigurer::disable)
                 .authorizeHttpRequests(auth->auth.requestMatchers("/api/v1/auth/**").permitAll())
                .authorizeHttpRequests(auth->auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .build()
                ;
    }


    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService){
        System.out.println("MON MANAGER");
        var authProvider= new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }



}
