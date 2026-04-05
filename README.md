# Cuddle Cup POS

A Java Swing point-of-sale system for **Cuddle Cup Coffee & Tea**. Includes admin dashboard, cashier checkout, inventory management, sales tracking, and receipt generation with QR codes.

---

## Prerequisites

Download and install all of the following before proceeding:

| Software | Version | Download Link |
|----------|---------|---------------|
| Java JDK | 17 or higher | https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html |
| XAMPP | Latest | https://www.apachefriends.org/download.html |
| Git | Latest | https://git-scm.com/downloads |

---

## Installation

### 1. Clone the project

```bash
git clone <your-repo-url>
cd java_shop
```

Or copy the entire `java_shop` folder to your device.

### 2. Start MySQL

1. Open **XAMPP Control Panel**
2. Click **Start** next to **MySQL**
3. Make sure it shows a green "Running" status on port **3306**

### 3. Create the database

Open a browser and go to http://localhost/phpmyadmin, then run this SQL:

```sql
CREATE DATABASE IF NOT EXISTS java_shop;
```

Or from terminal:

```bash
cd C:\xampp\mysql\bin
mysql -u root -e "CREATE DATABASE IF NOT EXISTS java_shop;"
```

### 4. Compile the project

Open a terminal in the `java_shop` folder and run:

```bash
javac -cp "src;lib/*" -d out src/config/*.java src/model/*.java src/util/*.java src/dao/*.java src/service/*.java src/ui/components/*.java src/ui/screens/cashier/*.java src/ui/screens/admin/*.java src/ui/screens/*.java src/ui/*.java src/App.java
```

### 5. Run the application

```bash
java -cp "out;lib/*" App
```

The app will auto-create all database tables and seed the menu products on first run.

---

## Default Login

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |

Create cashier accounts from the Admin panel after logging in.

---

## Project Structure

```
java_shop/
├── src/
│   ├── config/          AppConfig, DBConnection, Theme
│   ├── model/           Product, User, SaleItem, ReceiptSummary, etc.
│   ├── dao/             Database access (ProductDao, SalesDao, etc.)
│   ├── service/         Business logic (SalesService, AuthService, etc.)
│   ├── ui/
│   │   ├── components/  RoundedButton, RoundedPanel, UIFactory
│   │   └── screens/     LoginPanel, AdminPanel, CashierPanel, ReceiptDialog
│   ├── util/            CurrencyUtils, DateTimeUtils, ImageUtils, PasswordUtils
│   └── App.java         Entry point
├── lib/                 JAR dependencies (MySQL Connector, ZXing)
├── images/              logo.jpeg, menu.jpeg
└── README.md
```

---

## Libraries Included (in `lib/`)

- `mysql-connector-j-9.6.0.jar` — MySQL JDBC driver
- `core-3.5.3.jar` — ZXing barcode/QR code core
- `javase-3.5.3.jar` — ZXing Java SE utilities

No need to download these separately, they are already in the `lib/` folder.

---

## Troubleshooting

**"Access denied for user 'root'@'localhost'"**
XAMPP's default MySQL root password is empty. Make sure `DB_PASSWORD` in `src/config/AppConfig.java` is set to `""`.

**"Unknown database 'java_shop'"**
Create the database first (see step 3 above).

**Port 3306 already in use**
Another MySQL instance (Laragon, WSL, etc.) is running. Stop it first, or run `wsl --shutdown` if WSL is the cause.

**Application won't start**
Make sure you compiled first (step 4) and are running from the `java_shop` folder so the `images/` path resolves correctly.
