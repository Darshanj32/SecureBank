package securebank.storage;

import securebank.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Handles all file-based persistence for SecureBank.
 * Data is stored as plain, pipe-delimited text files under ./data
 * so it's easy to inspect and version (excluded from git via .gitignore).
 */
public class FileManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String ACCOUNTS_FILE = DATA_DIR + "/accounts.txt";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.txt";

    public FileManager() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            for (String f : new String[]{USERS_FILE, ACCOUNTS_FILE, TRANSACTIONS_FILE}) {
                Path p = Paths.get(f);
                if (!Files.exists(p)) Files.createFile(p);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize data directory: " + e.getMessage(), e);
        }
    }

    // ---------- USERS ----------
    // format: username|passwordHash|role|linkedAccountNumber
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        for (String line : readLines(USERS_FILE)) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            users.add(new User(p[0], p[1], Role.valueOf(p[2]), p[3].isEmpty() ? null : p[3]));
        }
        return users;
    }

    public void saveUsers(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join("|", u.getUsername(), u.getPasswordHash(),
                    u.getRole().name(), u.getLinkedAccountNumber() == null ? "" : u.getLinkedAccountNumber()));
        }
        writeLines(USERS_FILE, lines);
    }

    // ---------- ACCOUNTS ----------
    // format: accountNumber|type|ownerUsername|balance
    public List<Account> loadAccounts() {
        List<Account> accounts = new ArrayList<>();
        for (String line : readLines(ACCOUNTS_FILE)) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            String accNo = p[0], type = p[1], owner = p[2];
            double balance = Double.parseDouble(p[3]);
            Account acc = type.equals("SAVINGS")
                    ? new SavingsAccount(accNo, owner, balance)
                    : new CurrentAccount(accNo, owner, balance);
            accounts.add(acc);
        }
        return accounts;
    }

    public void saveAccounts(Collection<Account> accounts) {
        List<String> lines = new ArrayList<>();
        for (Account a : accounts) {
            lines.add(String.join("|", a.getAccountNumber(), a.getAccountType(),
                    a.getOwnerUsername(), String.valueOf(a.getBalance())));
        }
        writeLines(ACCOUNTS_FILE, lines);
    }

    // ---------- TRANSACTIONS ----------
    public void appendTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            bw.write(t.toFileLine());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Warning: could not persist transaction log: " + e.getMessage());
        }
    }

    public List<Transaction> loadTransactionsForAccount(String accountNumber) {
        List<Transaction> result = new ArrayList<>();
        for (String line : readLines(TRANSACTIONS_FILE)) {
            if (line.trim().isEmpty()) continue;
            Transaction t = Transaction.fromFileLine(line);
            if (t.getAccountNumber().equals(accountNumber)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> loadAllTransactions() {
        List<Transaction> result = new ArrayList<>();
        for (String line : readLines(TRANSACTIONS_FILE)) {
            if (line.trim().isEmpty()) continue;
            result.add(Transaction.fromFileLine(line));
        }
        return result;
    }

    // ---------- helpers ----------
    private List<String> readLines(String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void writeLines(String path, List<String> lines) {
        try {
            Files.write(Paths.get(path), lines);
        } catch (IOException e) {
            System.out.println("Warning: could not save to " + path + ": " + e.getMessage());
        }
    }
}
