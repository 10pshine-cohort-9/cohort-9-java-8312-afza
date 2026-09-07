package com.contactmanager.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_identifier", columnNames = "identifier"))
public class User {

    public enum IdentifierType {
        EMAIL,
        PHONE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IdentifierType identifierType;

    @Column(nullable = false, length = 60)
    private String passwordHash;

    protected User() {
    }

    public User(String firstName, String lastName, String identifier,
            IdentifierType identifierType, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
