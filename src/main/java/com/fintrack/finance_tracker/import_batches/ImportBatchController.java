package com.fintrack.finance_tracker.import_batches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/import-batches")
public class ImportBatchController {
    private final ImportBatchService importBatchService;

    @Autowired
    public ImportBatchController(ImportBatchService importBatchService) {
        this.importBatchService = importBatchService;
    }

    @GetMapping
    public List<ImportBatch> getImportBatches(
            @RequestParam (required = false) Integer id,
            @RequestParam (required = false) String fileName,
            @RequestParam (required = false) String status
    ) {
        if (id != null) {
            return importBatchService.getImportBatchById(id)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        else if (fileName != null) {
            return importBatchService.getImportBatchesByFileName(fileName);
        }

        else if (status != null) {
            return importBatchService.getImportBatchesByStatus(status);
        }

        else {
            return importBatchService.getImportBatches();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportBatch> getImportBatchById(@PathVariable int id) {
        return importBatchService.getImportBatchById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // TODO: POST /api/import-batches/upload (Upload a file for import, e.g., CSV)

    // TODO: GET /api/import-batches/{id}/transactions (Get transactions associated with a specific batch)

    // TODO: POST /api/import-batches/{id}/process (Manually trigger processing of an uploaded batch)

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBatch(@PathVariable int id) {
        importBatchService.deleteImportBatch(id);
        return new ResponseEntity<>("Import batch deleted successfully!", HttpStatus.OK);
    }
}
