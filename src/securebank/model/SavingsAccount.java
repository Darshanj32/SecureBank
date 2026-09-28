package securebank.model;

public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    private static final double MIN_BALANCE = 500.0;
    private double interestRate = 0.04; // 4% annual, illustrative only

    public SavingsAccount(String accountNumber, String ownerUsername, double initialBalance) {
        super(accountNumber, ownerUsername, initialBalance);
    }

    @Override
    protected boolean canWithdraw(double amount) {
        return getBalance() - amount >= MIN_BALANCE;
    }

    @Override
    public double getAvailableBalance() {
        return Math.max(0, getBalance() - MIN_BALANCE);
    }

    public double getInterestRate() { return interestRate; }

    @Override
    public String getAccountType() { return "SAVINGS"; }
}
