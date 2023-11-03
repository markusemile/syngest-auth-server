package be.markus.syngestbel.security.dao;


import be.markus.syngestbel.security.entity.Group;
import be.markus.syngestbel.security.entity.User;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupDAO extends JpaRepositoryImplementation<Group,Long> {

    Optional<Group> getGroupByName(String name);
    Optional<Group> getGroupById(Long id);

}
