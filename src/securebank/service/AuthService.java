package securebank.service;

import securebank.exception.InvalidLoginException;
import securebank.model.Role;
import securebank.model.User;
import securebank.storage.FileManager;
import securebank.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final FileManager fileManager;

    public AuthService(FileManager fileManager) {
        this.fileManager = fileManager;
        for (User u : fileManager.loadUsers()) {
            usersByUsername.put(u.getUsername(), u);
        }
        seedDefaultAdmin();
    }

    /** Ensures there is always at least one admin account: admin / admin123 */
    private void seedDefaultAdmin() {
        if (usersByUsername.values().stream().noneMatch(User::isAdmin)) {
            User admin = new User("admin", PasswordUtil.hash("admin123"), Role.ADMIN, null);
            usersByUsername.put(admin.getUsername(), admin);
            persist();
            System.out.println("[Setup] Default admin created -> username: admin | password: admin123");
        }
    }

    public User register(String username, String plainPassword, Role role) throws InvalidLoginException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidLoginException("Username cannot be empty.");
        }
        if (usersByUsername.containsKey(username)) {
            throw new InvalidLoginException("Username already exists.");
        }
        if (plainPassword == null || plainPassword.length() < 4) {
            throw new InvalidLoginException("Password must be at least 4 characters.");
        }
        User user = new User(username, PasswordUtil.hash(plainPassword), role, null);
        usersByUsername.put(username, user);
        persist();
        return user;
    }

    public User login(String username, String plainPassword) throws InvalidLoginException {
        User user = usersByUsername.get(username);
        if (user == null || !PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            throw new InvalidLoginException("Invalid username or password.");
        }
        return user;
    }

    public void linkAccount(String username, String accountNumber) {
        User u = usersByUsername.get(username);
        if (u != null) {
            u.setLinkedAccountNumber(accountNumber);
            persist();
        }
    }

    public void changePassword(String username, String newPlainPassword) {
        User u = usersByUsername.get(username);
        if (u != null) {
            u.setPasswordHash(PasswordUtil.hash(newPlainPassword));
            persist();
        }
    }

    public User getUser(String username) {
        return usersByUsername.get(username);
    }

    public void persist() {
        fileManager.saveUsers(usersByUsername.values());
    }
}
