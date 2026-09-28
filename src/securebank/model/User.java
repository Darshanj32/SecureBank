package securebank.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private String passwordHash;
    private final Role role;
    private String linkedAccountNumber; // null for admin users without a personal account

    public User(String username, String passwordHash, Role role, String linkedAccountNumber) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.linkedAccountNumber = linkedAccountNumber;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public String getLinkedAccountNumber() { return linkedAccountNumber; }
    public void setLinkedAccountNumber(String accNo) { this.linkedAccountNumber = accNo; }

    public boolean isAdmin() { return role == Role.ADMIN; }
}
