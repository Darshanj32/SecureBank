package securebank.model;

public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private double overdraftLimit = 1000.0;

    public CurrentAccount(String accountNumber, String ownerUsername, double initialBalance) {
        super(accountNumber, ownerUsername, initialBalance);
    }

    @Override
    protected boolean canWithdraw(double amount) {
        return getBalance() - amount >= -overdraftLimit;
    }

    @Override
    public double getAvailableBalance() {
        return getBalance() + overdraftLimit;
    }

    public double getOverdraftLimit() { return overdraftLimit; }

    @Override
    public String getAccountType() { return "CURRENT"; }
}
