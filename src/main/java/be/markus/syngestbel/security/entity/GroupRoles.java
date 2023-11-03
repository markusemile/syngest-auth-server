package be.markus.syngestbel.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "group_roles")
public class GroupRoles {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Group.class)
    @JoinColumn(name = "group_id",referencedColumnName = "id")
    private Group groupId;

    @ManyToOne(targetEntity = Role.class)
    @JoinColumn(name="role_id",referencedColumnName = "id")
    private Role roleId;


}
