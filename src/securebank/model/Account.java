package securebank.model;

import securebank.exception.InsufficientFundsException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accountNumber;
    private final String ownerUsername;
    private double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String accountNumber, String ownerUsername, double initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerUsername = ownerUsername;
        this.balance = initialBalance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerUsername() { return ownerUsername; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }

    public void deposit(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new Transaction(accountNumber, Transaction.Type.DEPOSIT,
                amount, balance, description));
    }

    public void withdraw(double amount, String description) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + getAvailableBalance());
        }
        balance -= amount;
        transactions.add(new Transaction(accountNumber, Transaction.Type.WITHDRAWAL,
                amount, balance, description));
    }

    /** Records a transaction without moving money again (used internally by services). */
    protected void recordTransaction(Transaction.Type type, double amount, String description) {
        transactions.add(new Transaction(accountNumber, type, amount, balance, description));
    }

    public void setBalanceDirect(double newBalance) {
        this.balance = newBalance;
    }

    public void addTransactionRecord(Transaction t) {
        this.transactions.add(t);
    }

    /** Hook for subclasses (e.g., CurrentAccount allows overdraft). */
    protected boolean canWithdraw(double amount) {
        return balance >= amount;
    }

    public double getAvailableBalance() {
        return balance;
    }

    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("%s | %s | Owner: %-10s | Balance: %.2f",
                accountNumber, getAccountType(), ownerUsername, balance);
    }
}
