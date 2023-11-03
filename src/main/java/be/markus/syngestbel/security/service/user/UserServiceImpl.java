package be.markus.syngestbel.security.service.user;

import be.markus.syngestbel.security.dao.UserDAO;
import be.markus.syngestbel.security.entity.User;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service

public class UserServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            UserDAO userDAO,
            PasswordEncoder passwordEncoder) {

        this.userDAO = userDAO;
        this.passwordEncoder=passwordEncoder;
    }

    public User loadUserByUsernameAndPassword(String username,String password) throws UsernameNotFoundException {
        Optional<User> uopt = this.userDAO.getUserByEmail(username);
        if(uopt.isPresent()){
            User u = uopt.get();
            String uPass = u.getPassword();
            System.out.println(uPass);
            boolean isMatch = passwordEncoder.matches(password,uPass);
            if(isMatch){
                return u;
            }else{
                throw new RuntimeException("Password not match !");
            }
        }else{
            throw new RuntimeException("User not found !");
        }

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.userDAO.getUserByEmail(email).orElse(null);
    }

    public boolean findUserIdentification(String ident){
        return this.userDAO.getUserByUserIdentification(ident).isPresent();
    }

    public User getUserByEmail(String email){
        return this.userDAO.getUserByEmail(email).orElse(null);
    }


    public User save(User u){
        return this.userDAO.save(u);
    }
}
