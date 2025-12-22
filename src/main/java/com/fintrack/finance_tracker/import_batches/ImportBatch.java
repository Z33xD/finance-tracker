package com.fintrack.finance_tracker.import_batches;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_batches")
public class ImportBatch {
    @Id
    @Column(name = "id")
    private int id;

    private int user_id;

    private String file_name;

    private String import_type;

    private String status;

    private int total_records;

    private int successful_records;

    private int failed_records;

    private LocalDateTime started_at;

    private LocalDateTime completed_at;

    private String error_message;

    public ImportBatch(int id, int user_id, String file_name, String import_type, String status, int total_records, int successful_records, int failed_records, LocalDateTime started_at, LocalDateTime completed_at, String error_message) {
        this.id = id;
        this.user_id = user_id;
        this.file_name = file_name;
        this.import_type = import_type;
        this.status = status;
        this.total_records = total_records;
        this.successful_records = successful_records;
        this.failed_records = failed_records;
        this.started_at = started_at;
        this.completed_at = completed_at;
        this.error_message = error_message;
    }

    public ImportBatch() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getImport_type() {
        return import_type;
    }

    public void setImport_type(String import_type) {
        this.import_type = import_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotal_records() {
        return total_records;
    }

    public void setTotal_records(int total_records) {
        this.total_records = total_records;
    }

    public int getSuccessful_records() {
        return successful_records;
    }

    public void setSuccessful_records(int successful_records) {
        this.successful_records = successful_records;
    }

    public int getFailed_records() {
        return failed_records;
    }

    public void setFailed_records(int failed_records) {
        this.failed_records = failed_records;
    }

    public LocalDateTime getStarted_at() {
        return started_at;
    }

    public void setStarted_at(LocalDateTime started_at) {
        this.started_at = started_at;
    }

    public LocalDateTime getCompleted_at() {
        return completed_at;
    }

    public void setCompleted_at(LocalDateTime completed_at) {
        this.completed_at = completed_at;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }
}
