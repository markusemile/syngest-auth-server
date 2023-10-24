package be.markus.syngestbel.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public SecurityConfig(JwtService jwtService,PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder=passwordEncoder;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

       return http
                .csrf(AbstractHttpConfigurer::disable)
                 .authorizeHttpRequests(auth->auth.requestMatchers("/token/**").permitAll())
                .authorizeHttpRequests(auth->auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .build()
                ;
    }


    @Bean
    public UserDetailsService inMemoryUserDetailsManager(){
        var user1 = User.withUsername("laura").password(passwordEncoder.encode("1234")).authorities("USER").build();
        var user2 = User.withUsername("emile").password(passwordEncoder.encode("1234")).authorities("ADMIN").build();
        var user3 = User.withUsername("sebi").password(passwordEncoder.encode("1234")).authorities("ADMIN","USER").build();
        var iudm = new InMemoryUserDetailsManager();
         iudm.createUser(user1);
         iudm.createUser(user2);
         iudm.createUser(user3);
         return iudm;
    }


    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService){
        var authProvider= new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }







}
