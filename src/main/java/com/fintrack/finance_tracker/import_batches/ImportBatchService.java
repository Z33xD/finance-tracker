package com.fintrack.finance_tracker.import_batches;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ImportBatchService {
    private final ImportBatchRepository importBatchRepository;

    @Autowired
    public ImportBatchService(ImportBatchRepository importBatchRepository) {
        this.importBatchRepository = importBatchRepository;
    }

    public Optional<ImportBatch> getImportBatchById(int searchKey) {
        return importBatchRepository.findById(searchKey);
    }

    public List<ImportBatch> getImportBatchesByFileName(String searchString) {
        return importBatchRepository.findAll().stream()
                .filter(importBatch -> importBatch.getFile_name().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<ImportBatch> getImportBatchesByStatus(String searchString) {
        return importBatchRepository.findAll().stream()
                .filter(importBatch -> importBatch.getStatus().equalsIgnoreCase(searchString))
                .collect(Collectors.toList());
    }

    public ImportBatch addImportBatch(ImportBatch importBatch) {
        importBatchRepository.save(importBatch);
        return importBatch;
    }

    public ImportBatch updateImportBatch(ImportBatch updatedImportBatch) {
        Optional<ImportBatch> existingImportBatch = importBatchRepository.findById(updatedImportBatch.getId());

        if (existingImportBatch.isPresent()) {
            ImportBatch importBatchToUpdate = existingImportBatch.get();
            importBatchToUpdate.setUser_id(updatedImportBatch.getUser_id());
            importBatchToUpdate.setFile_name(updatedImportBatch.getFile_name());
            importBatchToUpdate.setImport_type(updatedImportBatch.getImport_type());
            importBatchToUpdate.setStatus(updatedImportBatch.getStatus());
            importBatchToUpdate.setTotal_records(updatedImportBatch.getTotal_records());
            importBatchToUpdate.setSuccessful_records(updatedImportBatch.getSuccessful_records());
            importBatchToUpdate.setFailed_records(updatedImportBatch.getFailed_records());
            importBatchToUpdate.setError_message(updatedImportBatch.getError_message());

            importBatchRepository.save(importBatchToUpdate);
            return importBatchToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteImportBatch(int id) {
        importBatchRepository.deleteById(id);
    }
}
