package com.fintrack.finance_tracker.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/accounts/")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController (AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAccounts(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer user_id,
            @RequestParam(required = false) String account_type
    ) {
        if (id != null) {
            return accountService.getAccountById(id)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        else if (user_id != null) {
            return accountService.getAccountsByUserId(user_id);
        }

        else if (account_type != null) {
            return accountService.getAccountsByAccountType(account_type);
        }

        else {
            return accountService.getAccounts();
        }
    }

    // TODO: GET /api/accounts (Retrieve all accounts for the authenticated user)

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable int id) {
        return accountService.getAccountById(id)
                .map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Account> addAccount(@RequestBody Account account) {
        Account createdAccount = accountService.addAccount(account);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccountById(@PathVariable int id, @RequestBody Account account) {
        Account updatedAccount = accountService.updateAccount(id, account);
        if (updatedAccount != null) {
            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable int id) {
        accountService.deleteAccount(id);
        return new ResponseEntity<>("Account deleted successfully!", HttpStatus.OK);
    }

    // TODO: DELETE /api/users/me (Delete currently authenticated user's account)
}
