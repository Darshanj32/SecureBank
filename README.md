# SecureBank (Java Edition)

A console-based banking management system in Java, ported from the original C++/OOP
version. Features account management, user authentication, transactions, bill
payments, admin controls, file-based data persistence, and masked password input.

## Features

- **Authentication**: register/login, SHA-256 password hashing, masked password entry
- **Accounts**: Savings (minimum balance rule) and Current (overdraft limit) accounts
- **Transactions**: deposit, withdraw, transfer between accounts, transaction history
- **Bill payments**: pay bills against categories (electricity, water, internet, etc.)
- **Admin controls**: list all accounts, view total bank balance, view all transactions
- **Persistence**: plain-text pipe-delimited files under `data/` (users, accounts, transactions)

## Project structure

```
SecureBank/
├── src/
│   ├── Main.java
│   └── securebank/
│       ├── model/       (User, Account, SavingsAccount, CurrentAccount, Transaction, Bill, Role)
│       ├── service/     (AuthService, AccountService, TransactionService, AdminService)
│       ├── storage/     (FileManager)
│       ├── util/        (PasswordUtil)
│       └── exception/   (InsufficientFundsException, InvalidLoginException, AccountNotFoundException)
├── Dockerfile
├── .github/workflows/build.yml
└── README.md
```

## Requirements

- JDK 17 or newer (works with JDK 8+ if you remove `var`/newer syntax, but this
  project only uses standard APIs available since Java 8, so JDK 8+ is fine too)

Check your version:
```bash
java -version
javac -version
```

## 1. Run it locally (from source, no build tool needed)

```bash
# From the project root (the folder containing src/)
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Run (use a real terminal, not an IDE's console, so password masking works)
java -cp out Main
```

On first run, a default admin is created automatically:
```
username: admin
password: admin123
```
Change this password immediately after your first login (option "Change password"
is on the customer menu; for the admin account you can also edit `data/users.txt`
directly by replacing the hash — advanced/optional).

Data is stored in `./data/*.txt` and persists between runs. Delete the `data/`
folder to reset the system.

## 2. Run it with Docker

```bash
# Build the image
docker build -t securebank .

# Run interactively, with a named volume so data survives container restarts
docker run -it --name securebank -v securebank-data:/app/data securebank
```

To run it again later:
```bash
docker start -ai securebank
```

## 3. Push this project to GitHub

From the project root:

```bash
git init
git add .
git commit -m "Initial commit: SecureBank Java banking system"
git branch -M main
git remote add origin https://github.com/<your-username>/SecureBank.git
git push -u origin main
```

Replace `<your-username>` with your GitHub username, and create the empty
`SecureBank` repository on GitHub first (no README/license, so it doesn't
conflict with this push).

A GitHub Actions workflow (`.github/workflows/build.yml`) is included and will
automatically compile the project (and run a smoke test) on every push or pull
request to `main`.

## 4. Deploy it

This is a console/terminal application (stdin/stdout), so "deploying" it means
making it runnable somewhere, not hosting it as a web service. Common options:

- **A VM / server (e.g., AWS EC2, DigitalOcean droplet, Azure VM)**: install a
  JDK, clone the repo, compile, and run it inside a `screen` or `tmux` session
  (or as a `systemd` service) so it keeps running after you disconnect.
- **Docker anywhere** (a server, or a container platform that supports
  interactive/TTY containers): build and run the image as shown above.
- **GitHub Codespaces**: open the repo in a Codespace, compile, and run —
  gives you a full Linux terminal in the browser with nothing to install.

Example: deploy on a fresh Linux server via SSH:
```bash
sudo apt update && sudo apt install -y default-jdk git   # Debian/Ubuntu
git clone https://github.com/<your-username>/SecureBank.git
cd SecureBank
mkdir -p out && find src -name "*.java" > sources.txt && javac -d out @sources.txt
java -cp out Main
```

To keep it running in the background over SSH, use `tmux`:
```bash
tmux new -s securebank
java -cp out Main
# Ctrl+B then D to detach; `tmux attach -t securebank` to come back
```

## Notes on password masking

`System.console()` (used for masked input) returns `null` when run inside most
IDE consoles or with redirected input. The app detects this and falls back to
visible `Scanner` input with a warning, so it still runs everywhere — for real
masking, run it from an actual terminal (as shown above).
