import securebank.exception.AccountNotFoundException;
import securebank.exception.InsufficientFundsException;
import securebank.exception.InvalidLoginException;
import securebank.model.*;
import securebank.service.*;
import securebank.storage.FileManager;
import securebank.util.PasswordUtil;

import java.util.List;
import java.util.Scanner;

/**
 * SecureBank - Console-Based Banking Management System (Java edition)
 * Entry point: menus for authentication, customer banking, and admin controls.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static FileManager fileManager;
    private static AuthService authService;
    private static AccountService accountService;
    private static TransactionService transactionService;
    private static AdminService adminService;

    private static User currentUser = null;

    public static void main(String[] args) {
        fileManager = new FileManager();
        authService = new AuthService(fileManager);
        accountService = new AccountService(fileManager);
        transactionService = new TransactionService(accountService, fileManager);
        adminService = new AdminService(accountService, fileManager);

        System.out.println("=========================================");
        System.out.println("      Welcome to SecureBank (Java)      ");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showWelcomeMenu();
            } else if (currentUser.isAdmin()) {
                showAdminMenu();
            } else {
                showCustomerMenu();
            }
        }

        System.out.println("Thank you for using SecureBank. Goodbye!");
        scanner.close();
    }

    // ---------------- Welcome / Auth ----------------

    private static boolean showWelcomeMenu() {
        System.out.println("\n1. Login\n2. Register\n3. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                login();
                return true;
            case "2":
                register();
                return true;
            case "3":
                return false;
            default:
                System.out.println("Invalid option.");
                return true;
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        String password = PasswordUtil.readPassword(scanner, "Password: ");

        try {
            currentUser = authService.login(username, password);
            System.out.println("Login successful. Welcome, " + username + "!");
        } catch (InvalidLoginException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void register() {
        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();
        String password = PasswordUtil.readPassword(scanner, "Choose a password: ");

        try {
            User user = authService.register(username, password, Role.CUSTOMER);

            System.out.println("Open your first account.");
            System.out.print("Account type (SAVINGS/CURRENT): ");
            String type = scanner.nextLine().trim();
            System.out.print("Initial deposit amount: ");
            double initial = readDouble();

            Account account = accountService.openAccount(username, type, initial);
            authService.linkAccount(username, account.getAccountNumber());

            System.out.println("Registration successful! Your account number is: "
                    + account.getAccountNumber());
        } catch (InvalidLoginException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // ---------------- Customer menu ----------------

    private static void showCustomerMenu() {
        System.out.println("\n--- Customer Menu (" + currentUser.getUsername() + ") ---");
        System.out.println("1. View my accounts");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Pay a bill");
        System.out.println("6. Transaction history");
        System.out.println("7. Open a new account");
        System.out.println("8. Change password");
        System.out.println("9. Logout");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1": viewMyAccounts(); break;
                case "2": doDeposit(); break;
                case "3": doWithdraw(); break;
                case "4": doTransfer(); break;
                case "5": doPayBill(); break;
                case "6": doHistory(); break;
                case "7": doOpenAccount(); break;
                case "8": doChangePassword(); break;
                case "9": currentUser = null; break;
                default: System.out.println("Invalid option.");
            }
        } catch (AccountNotFoundException | InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewMyAccounts() throws AccountNotFoundException {
        String accNo = requireLinkedAccount();
        System.out.println(accountService.getAccount(accNo));
    }

    private static void doDeposit() throws AccountNotFoundException {
        String accNo = requireLinkedAccount();
        System.out.print("Amount to deposit: ");
        double amount = readDouble();
        transactionService.deposit(accNo, amount);
        System.out.println("Deposit successful. New balance: "
                + accountService.getAccount(accNo).getBalance());
    }

    private static void doWithdraw() throws AccountNotFoundException, InsufficientFundsException {
        String accNo = requireLinkedAccount();
        System.out.print("Amount to withdraw: ");
        double amount = readDouble();
        transactionService.withdraw(accNo, amount);
        System.out.println("Withdrawal successful. New balance: "
                + accountService.getAccount(accNo).getBalance());
    }

    private static void doTransfer() throws AccountNotFoundException, InsufficientFundsException {
        String fromAcc = requireLinkedAccount();
        System.out.print("Recipient account number: ");
        String toAcc = scanner.nextLine().trim();
        System.out.print("Amount to transfer: ");
        double amount = readDouble();
        transactionService.transfer(fromAcc, toAcc, amount);
        System.out.println("Transfer successful.");
    }

    private static void doPayBill() throws AccountNotFoundException, InsufficientFundsException {
        String accNo = requireLinkedAccount();
        System.out.print("Biller name: ");
        String biller = scanner.nextLine().trim();
        System.out.println("Category: 1.ELECTRICITY 2.WATER 3.INTERNET 4.MOBILE 5.CREDIT_CARD 6.OTHER");
        System.out.print("Choose category: ");
        Bill.Category category = mapBillCategory(scanner.nextLine().trim());
        System.out.print("Amount: ");
        double amount = readDouble();

        Bill bill = new Bill(biller, category, amount);
        transactionService.payBill(accNo, bill);
        System.out.println("Bill paid successfully.");
    }

    private static Bill.Category mapBillCategory(String choice) {
        switch (choice) {
            case "1": return Bill.Category.ELECTRICITY;
            case "2": return Bill.Category.WATER;
            case "3": return Bill.Category.INTERNET;
            case "4": return Bill.Category.MOBILE;
            case "5": return Bill.Category.CREDIT_CARD;
            default: return Bill.Category.OTHER;
        }
    }

    private static void doHistory() {
        String accNo = currentUser.getLinkedAccountNumber();
        List<securebank.model.Transaction> history = transactionService.getHistory(accNo);
        System.out.println("\n--- Transaction History for " + accNo + " ---");
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
        }
        for (securebank.model.Transaction t : history) {
            System.out.println(t);
        }
    }

    private static void doOpenAccount() {
        System.out.print("Account type (SAVINGS/CURRENT): ");
        String type = scanner.nextLine().trim();
        System.out.print("Initial deposit amount: ");
        double initial = readDouble();
        Account account = accountService.openAccount(currentUser.getUsername(), type, initial);
        System.out.println("New account opened: " + account.getAccountNumber());
        System.out.print("Make this your primary linked account? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            authService.linkAccount(currentUser.getUsername(), account.getAccountNumber());
        }
    }

    private static void doChangePassword() {
        String newPassword = PasswordUtil.readPassword(scanner, "New password: ");
        authService.changePassword(currentUser.getUsername(), newPassword);
        System.out.println("Password updated.");
    }

    private static String requireLinkedAccount() throws AccountNotFoundException {
        String accNo = currentUser.getLinkedAccountNumber();
        if (accNo == null) {
            throw new AccountNotFoundException("No account linked to this user yet. Open one first.");
        }
        return accNo;
    }

    // ---------------- Admin menu ----------------

    private static void showAdminMenu() {
        System.out.println("\n--- Admin Menu (" + currentUser.getUsername() + ") ---");
        System.out.println("1. List all accounts");
        System.out.println("2. View total bank balance");
        System.out.println("3. View all transactions");
        System.out.println("4. Logout");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": adminService.listAllAccounts(); break;
            case "2":
                System.out.printf("Total balance across all accounts: %.2f%n",
                        adminService.getTotalBankBalance());
                break;
            case "3": adminService.printAllTransactions(); break;
            case "4": currentUser = null; break;
            default: System.out.println("Invalid option.");
        }
    }

    // ---------------- Helpers ----------------

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
