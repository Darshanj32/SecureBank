package securebank.service;

import securebank.model.Account;
import securebank.model.Transaction;
import securebank.storage.FileManager;

import java.util.List;

public class AdminService {
    private final AccountService accountService;
    private final FileManager fileManager;

    public AdminService(AccountService accountService, FileManager fileManager) {
        this.accountService = accountService;
        this.fileManager = fileManager;
    }

    public void listAllAccounts() {
        System.out.println("\n--- All Accounts ---");
        boolean any = false;
        for (Account a : accountService.getAllAccounts()) {
            System.out.println(a);
            any = true;
        }
        if (!any) System.out.println("No accounts yet.");
    }

    public double getTotalBankBalance() {
        double total = 0;
        for (Account a : accountService.getAllAccounts()) {
            total += a.getBalance();
        }
        return total;
    }

    public void printAllTransactions() {
        List<Transaction> all = fileManager.loadAllTransactions();
        System.out.println("\n--- All Transactions (" + all.size() + ") ---");
        for (Transaction t : all) {
            System.out.println(t);
        }
    }
}
