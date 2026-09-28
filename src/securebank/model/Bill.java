package securebank.model;

import java.io.Serializable;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Category { ELECTRICITY, WATER, INTERNET, MOBILE, CREDIT_CARD, OTHER }

    private final String billerName;
    private final Category category;
    private final double amount;

    public Bill(String billerName, Category category, double amount) {
        this.billerName = billerName;
        this.category = category;
        this.amount = amount;
    }

    public String getBillerName() { return billerName; }
    public Category getCategory() { return category; }
    public double getAmount() { return amount; }
}
