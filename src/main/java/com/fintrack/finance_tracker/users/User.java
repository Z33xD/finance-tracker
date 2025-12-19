package com.fintrack.finance_tracker.users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.cglib.core.Local;

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

    public User(int id, String username, String email, String password_hash, LocalDateTime created_at) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password_hash = password_hash;
        this.created_at = created_at;
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
}
