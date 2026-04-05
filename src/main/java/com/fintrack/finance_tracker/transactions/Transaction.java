package com.fintrack.finance_tracker.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "id", unique = true)
    private int id;

    @Column(name = "account_id")
    private int accountId;

    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "amount")
    private double amount;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "import_batch_id")
    private int importBatchId;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    public Transaction(int id, int accountId, int categoryId, double amount, LocalDate transactionDate, String description, String transactionType, int importBatchId, LocalDateTime datetime) {
        this.id = id;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.transactionType = transactionType;
        this.importBatchId = importBatchId;
        this.datetime = datetime;
    }

    public Transaction() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccount_id() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getImportBatchId() {
        return importBatchId;
    }

    public void setImportBatchId(int importBatchId) {
        this.importBatchId = importBatchId;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }
}
