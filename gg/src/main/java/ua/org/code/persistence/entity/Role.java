package ua.org.code.persistence.entity;

import ua.org.code.annotation.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "roles")
public class Role {
    Long id;
    String name;
}
