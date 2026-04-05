package com.fintrack.finance_tracker.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/users/")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email
    ) {
        if (id != null) {
            return userService.getUserById(id)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
        else if (name != null) {
            return userService.getUsersByName(name);
        }
        else if (email != null) {
            return userService.getUsersByEmail(email);
        }
        else {
            return userService.getUsers();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = userService.getUserById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(user);
    }

    // Retrieve currently authenticated user's details
    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        User currentUser = getAuthenticatedUser();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        User createdUser = userService.addUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserById(@PathVariable int id, @RequestBody User user) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User updatedUser = userService.updateUser(id, user);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    //  Update currently authenticated user's profile
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody User updatedData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User currentUser = (User) authentication.getPrincipal();

        assert currentUser != null;
        updatedData.setId(currentUser.getId());
        User updatedUser = userService.updateUser(currentUser.getId(), updatedData);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // Delete currently authenticated user's account
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User currentUser = (User) authentication.getPrincipal();
        assert currentUser != null;
        userService.deleteUser(currentUser.getId());

        return ResponseEntity.ok("Your account has been deleted successfully!");
    }

    // Helper method to get the authenticated user
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return (User) authentication.getPrincipal();
    }
}
