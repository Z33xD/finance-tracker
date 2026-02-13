package com.fintrack.finance_tracker.users;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(int searchKey) {
        return userRepository.findById(searchKey);
    }

    public List<User> getUsersByName(String searchText) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<User> getUsersByEmail(String searchText) {
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    public User addUser(User user) {
        userRepository.save(user);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        return user;
    }

    public User updateUser(int id, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();

            userToUpdate.setUsername(updatedUser.getUsername());
            if (updatedUser.getEmail() != null) {
                userToUpdate.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPasswordHash() != null) {
                userToUpdate.setPasswordHash(updatedUser.getPasswordHash());
            }

            userRepository.save(userToUpdate);
            return userToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }
}
