package be.markus.syngestbel.security.dao;


import be.markus.syngestbel.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDAO extends JpaRepositoryImplementation<User,Long> {


    Optional<User> getUserByEmailAndPasswordAndIsEnableTrue(String email,String password);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUserIdentification(String ident);

}
