package com.fintrack.finance_tracker.users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
public class User {
    @Id
    @Column(name = "id", unique = true)
    private int id;

    private String username;

    private String email;

    private String password_hash;

    private LocalDateTime created_at;

    private boolean enabled;

    private String verification_code;

    private LocalDateTime verification_expiration;

    public User(int id, String username, String email, String password_hash, LocalDateTime created_at, boolean enabled, String verification_code, LocalDateTime verification_expiration) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password_hash = password_hash;
        this.created_at = created_at;
        this.enabled = enabled;
        this.verification_code = verification_code;
        this.verification_expiration = verification_expiration;
    }

    public User() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVerification_code() {
        return verification_code;
    }

    public void setVerification_code(String verification_code) {
        this.verification_code = verification_code;
    }

    public LocalDateTime getVerification_expiration() {
        return verification_expiration;
    }

    public void setVerification_expiration(LocalDateTime verification_expiration) {
        this.verification_expiration = verification_expiration;
    }

}
