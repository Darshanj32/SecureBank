package securebank.service;

import securebank.exception.AccountNotFoundException;
import securebank.model.Account;
import securebank.model.CurrentAccount;
import securebank.model.SavingsAccount;
import securebank.storage.FileManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AccountService {
    private final Map<String, Account> accountsByNumber = new HashMap<>();
    private final FileManager fileManager;
    private final Random random = new Random();

    public AccountService(FileManager fileManager) {
        this.fileManager = fileManager;
        for (Account a : fileManager.loadAccounts()) {
            accountsByNumber.put(a.getAccountNumber(), a);
        }
    }

    public Account openAccount(String ownerUsername, String type, double initialDeposit) {
        String accNo = generateAccountNumber();
        Account account = type.equalsIgnoreCase("SAVINGS")
                ? new SavingsAccount(accNo, ownerUsername, initialDeposit)
                : new CurrentAccount(accNo, ownerUsername, initialDeposit);
        accountsByNumber.put(accNo, account);
        persist();
        return account;
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account a = accountsByNumber.get(accountNumber);
        if (a == null) throw new AccountNotFoundException("No account found with number: " + accountNumber);
        return a;
    }

    public Iterable<Account> getAllAccounts() {
        return accountsByNumber.values();
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = "SB" + (100000 + random.nextInt(900000));
        } while (accountsByNumber.containsKey(candidate));
        return candidate;
    }

    public void persist() {
        fileManager.saveAccounts(accountsByNumber.values());
    }
}
