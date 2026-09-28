package securebank.service;

import securebank.exception.AccountNotFoundException;
import securebank.exception.InsufficientFundsException;
import securebank.model.Account;
import securebank.model.Bill;
import securebank.model.Transaction;
import securebank.storage.FileManager;

import java.util.List;

public class TransactionService {
    private final AccountService accountService;
    private final FileManager fileManager;

    public TransactionService(AccountService accountService, FileManager fileManager) {
        this.accountService = accountService;
        this.fileManager = fileManager;
    }

    public void deposit(String accountNumber, double amount) throws AccountNotFoundException {
        Account acc = accountService.getAccount(accountNumber);
        acc.deposit(amount, "Cash deposit");
        accountService.persist();
        fileManager.appendTransaction(lastTransaction(acc));
    }

    public void withdraw(String accountNumber, double amount)
            throws AccountNotFoundException, InsufficientFundsException {
        Account acc = accountService.getAccount(accountNumber);
        acc.withdraw(amount, "Cash withdrawal");
        accountService.persist();
        fileManager.appendTransaction(lastTransaction(acc));
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws AccountNotFoundException, InsufficientFundsException {
        Account from = accountService.getAccount(fromAccountNumber);
        Account to = accountService.getAccount(toAccountNumber);

        from.withdraw(amount, "Transfer to " + toAccountNumber);
        to.deposit(amount, "Transfer from " + fromAccountNumber);

        accountService.persist();
        fileManager.appendTransaction(lastTransaction(from));
        fileManager.appendTransaction(lastTransaction(to));
    }

    public void payBill(String accountNumber, Bill bill)
            throws AccountNotFoundException, InsufficientFundsException {
        Account acc = accountService.getAccount(accountNumber);
        acc.withdraw(bill.getAmount(), "Bill payment: " + bill.getBillerName() + " (" + bill.getCategory() + ")");
        accountService.persist();
        fileManager.appendTransaction(lastTransaction(acc));
    }

    public List<Transaction> getHistory(String accountNumber) {
        return fileManager.loadTransactionsForAccount(accountNumber);
    }

    private Transaction lastTransaction(Account acc) {
        List<Transaction> txns = acc.getTransactions();
        return txns.get(txns.size() - 1);
    }
}
