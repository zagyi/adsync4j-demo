package org.adsync4j.demo.entity;

import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.UUID;

/**
 * JPA entity class that represents an employee. Employees are users synchronized from Active Directory.
 */
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String ldapId;

    private String name;

    @Column(unique = true)
    private String email;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("id", id)
                      .add("ldapId", ldapId)
                      .add("name", name)
                      .add("email", email)
                      .toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Employee &&
               Objects.equal(email, ((Employee) o).getEmail());
    }

    @Override
    public int hashCode() {
        return email == null ? 0 : email.hashCode();
    }

    public Long getId() {
        return id;
    }

    public Employee setId(Long id) {
        this.id = id;
        return this;
    }

    public UUID getLdapId() {
        return UUID.fromString(ldapId);
    }

    public Employee setLdapId(UUID ldapId) {
        this.ldapId = ldapId.toString();
        return this;
    }

    public String getName() {
        return name;
    }

    public Employee setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Employee setEmail(String email) {
        this.email = email;
        return this;
    }
}
