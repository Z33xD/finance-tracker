package com.fintrack.finance_tracker.import_batches;

import com.fintrack.finance_tracker.transactions.TransactionService;
import com.fintrack.finance_tracker.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/import-batches/")
public class ImportBatchController {
    private final ImportBatchService importBatchService;
    private final TransactionService transactionService;

    @Autowired
    public ImportBatchController(ImportBatchService importBatchService, TransactionService transactionService) {
        this.importBatchService = importBatchService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<ImportBatch> getImportBatches(
            @RequestParam (required = false) Integer id,
            @RequestParam (required = false) String fileName,
            @RequestParam (required = false) String status
    ) {
        User currentUser = getAuthenticatedUser();

        if (id != null) {
            return importBatchService.getImportBatchByIdForUser(id, currentUser.getId())
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        else if (fileName != null) {
            return importBatchService.getImportBatchesByFileName(currentUser.getId(), fileName);
        }

        else if (status != null) {
            return importBatchService.getImportBatchesByStatus(currentUser.getId(), status);
        }

        else {
            return importBatchService.getImportBatchesForUser(currentUser.getId());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportBatch> getImportBatchById(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        return importBatchService.getImportBatchByIdForUser(id, currentUser.getId())
                .map(batch -> new ResponseEntity<>(batch, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportBatch> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("account_id") int accountId) {
        User currentUser = getAuthenticatedUser();

        if (!transactionService.isAccountOwnedByUser(accountId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        ImportBatch batch = importBatchService.processCsv(file, accountId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }

    // TODO: GET /api/import-batches/{id}/transactions (Get transactions associated with a specific batch)

    // TODO: POST /api/import-batches/{id}/process (Manually trigger processing of an uploaded batch)

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBatch(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        importBatchService.getImportBatchByIdForUser(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        importBatchService.deleteImportBatch(id, currentUser.getId());
        return new ResponseEntity<>("Import batch deleted successfully!", HttpStatus.OK);
    }

    // Helper method to get the authenticated user
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return (User) principal;
    }
}
