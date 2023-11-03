package be.markus.syngestbel.security.service.group;

import be.markus.syngestbel.security.dao.GroupDAO;
import be.markus.syngestbel.security.entity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service

public class GroupServiceImpl {
    private final GroupDAO groupDAO;

    @Autowired
    public GroupServiceImpl(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public Optional<Group> findByName(String name){
        return this.groupDAO.getGroupByName(name);
    }

    public Optional<Group> findById(Long id){
        return this.groupDAO.getGroupById(id);
    }

}
