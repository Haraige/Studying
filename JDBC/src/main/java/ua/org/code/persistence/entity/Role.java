package ua.org.code.persistence.entity;

import annotation.Table;
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
