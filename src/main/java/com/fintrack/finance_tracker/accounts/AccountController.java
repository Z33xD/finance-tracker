package com.fintrack.finance_tracker.accounts;

import com.fintrack.finance_tracker.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/accounts")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController (AccountService accountService) {
        this.accountService = accountService;
    }

    // Retrieve all accounts for the authenticated user
    @GetMapping
    public List<Account> getAccountsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return accountService.getAccountsByUserId(currentUser.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();
        Account account = accountService.getAccountById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (account.getUserId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<Account> addAccount(@RequestBody Account account) {
        System.out.println("addAccount() hit");
        User currentUser = getAuthenticatedUser();
        System.out.println("Authenticated User ID: " + currentUser.getId());
        account.setUserId(currentUser.getId());

        Account createdAccount = accountService.addAccount(account);
        System.out.println(account.getAccountName());
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccountById(@PathVariable int id, @RequestBody Account account) {
        User currentUser = getAuthenticatedUser();

        Account existing = accountService.getAccountById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existing.getUserId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        account.setUserId(currentUser.getId());

        Account updatedAccount = accountService.updateAccount(id, account);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();
        Account existing = accountService.getAccountById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existing.getUserId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        accountService.deleteAccount(id);
        return ResponseEntity.ok("Account deleted successfully!");
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
