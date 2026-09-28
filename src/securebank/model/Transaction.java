package securebank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, BILL_PAYMENT }

    private final String accountNumber;
    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String description;

    public Transaction(String accountNumber, Type type, double amount,
                        double balanceAfter, String description) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public String getAccountNumber() { return accountNumber; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[%s] %-14s %-10.2f  Balance: %-10.2f  %s",
                timestamp.format(FORMATTER), type, amount, balanceAfter, description);
    }

    /** Serializes this transaction to a single pipe-delimited line for file storage. */
    public String toFileLine() {
        return String.join("|",
                accountNumber,
                type.name(),
                String.valueOf(amount),
                String.valueOf(balanceAfter),
                timestamp.format(FORMATTER),
                description.replace("|", "/"));
    }

    public static Transaction fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        Transaction t = new Transaction(parts[0], Type.valueOf(parts[1]),
                Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), parts[5]);
        return t;
    }
}
