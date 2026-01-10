package com.fintrack.finance_tracker.import_batches;

import com.fintrack.finance_tracker.transactions.Transaction;
import com.fintrack.finance_tracker.transactions.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ImportBatchService {
    private final ImportBatchRepository importBatchRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public ImportBatchService(ImportBatchRepository importBatchRepository, TransactionRepository transactionRepository) {
        this.importBatchRepository = importBatchRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<ImportBatch> getImportBatches() {
        return importBatchRepository.findAll();
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

    public ImportBatch processCsv(MultipartFile file) {
        ImportBatch batch = new ImportBatch();
        batch.setFile_name(file.getOriginalFilename());
        batch.setStatus("Pending");
        batch.setStarted_at(LocalDateTime.now());

        importBatchRepository.save(batch);

        try {
            parseCsv(file.getInputStream(), batch);
            batch.setStatus("Processed");
            batch.setCompleted_at(LocalDateTime.now());
        } catch (Exception e) {
            batch.setStatus("Failed");
            batch.setError_message(e.getMessage());
        }

        return importBatchRepository.save(batch);
    }

    private void parseCsv(InputStream inputStream, ImportBatch batch) throws IOException {

        int total = 0;
        int success = 0;
        int failed = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .get();

            CSVParser parser = format.parse(reader);

            for (CSVRecord record : parser) {
                total++;

                try {
                    String categoryIdStr = record.get("category_id");
                    String amountStr = record.get("amount");
                    String transactionDateStr = record.get("transaction_date"); // make sure to specify this format somewhere
                    String description = record.get("description");
                    String transactionType = record.get("transaction_type");

                    int categoryId = Integer.parseInt(categoryIdStr);
                    double amount = Double.parseDouble(amountStr.replace(",", ""));
                    LocalDate transactionDate = LocalDate.parse(transactionDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    Transaction transaction = new Transaction();
                    transaction.setCategory_id(categoryId);
                    transaction.setAmount(amount);
                    transaction.setTransaction_date(transactionDate);
                    transaction.setDescription(description);
                    transaction.setTransaction_type(transactionType);

                    transaction.setImport_batch_id(batch.getId());

                    transactionRepository.save(transaction);
                    success++;
                } catch (Exception e) {
                    failed++;
                }
            }
        }

        batch.setTotal_records(total);
        batch.setSuccessful_records(success);
        batch.setFailed_records(failed);
    }
}
