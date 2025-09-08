[README.md](https://github.com/user-attachments/files/22202372/README.md)
# Pahana Edu Billing System

A lightweight **Java (JSP + Servlets)** web application for a bookshop-style billing platform. It implements clean **MVC + DAO** architecture with **MySQL** for persistence, session-based authentication, and core modules for **Customers, Items, Bills, and Reports**.

---

## ✨ Features

- **Secure Login & Sessions** (role-ready)
- **Customer Management**: add, edit, delete, search
- **Item Management**: add, edit, delete, stock/price
- **Bill Generation**: cart-style billing, total/discount/tax
- **Monthly Reports**: filter by date ranges
- **Audit Logs**: login activity & bill history
- **Validation**: server-side and JSP-level checks
- **Clean Architecture**: MVC, DAO, Singleton (DB connection), Strategy-ready services

---

## 🏗️ Tech Stack

- **Backend:** Java 17, JSP, Servlets, JSTL
- **Web/App Server:** Apache Tomcat 9/10
- **Build:** Maven
- **Database:** MySQL 8.x
- **Testing:** JUnit
- **IDE (example):** IntelliJ IDEA / Eclipse

> The app is framework-free (no Spring), designed for clarity in an academic/learning context.

---

## 📁 Project Structure

```
pahana-edu-billing/
├─ src/
│  ├─ main/
│  │  ├─ java/com/pahanaedu/
│  │  │  ├─ controller/         # Servlets (LoginServlet, CustomerServlet, ItemServlet, BillServlet, ReportServlet)
│  │  │  ├─ dao/                # DAO classes (CustomerDAO, ItemDAO, BillDAO, UserDAO)
│  │  │  ├─ model/              # POJOs/DTOs (Customer, Item, Bill, BillItem, User)
│  │  │  ├─ service/            # Business logic (optional Strategy-ready services)
│  │  │  └─ util/               # DBConnection (Singleton), validators, helpers
│  │  ├─ resources/
│  │  │  └─ db.properties       # DB configs (see below)
│  │  └─ webapp/
│  │     ├─ WEB-INF/web.xml     # Servlet mappings
│  │     ├─ index.jsp
│  │     ├─ login.jsp
│  │     ├─ customers/*.jsp
│  │     ├─ items/*.jsp
│  │     ├─ bills/*.jsp
│  │     └─ reports/*.jsp
│  └─ test/java/...              # JUnit tests
├─ pom.xml
└─ README.md
```

---

## 🗄️ Database

**Schema name:** `pahana_edu`

Minimal SQL to get started:

```sql
CREATE DATABASE IF NOT EXISTS pahana_edu;
USE pahana_edu;

-- Users
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers
CREATE TABLE IF NOT EXISTS customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  address VARCHAR(255),
  created_date DATE DEFAULT (CURRENT_DATE)
);

-- Items
CREATE TABLE IF NOT EXISTS items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bills (header)
CREATE TABLE IF NOT EXISTS bills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT,
  sub_total DECIMAL(10,2) NOT NULL,
  discount DECIMAL(10,2) DEFAULT 0,
  tax DECIMAL(10,2) DEFAULT 0,
  total DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Bill Items (lines)
CREATE TABLE IF NOT EXISTS bill_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  bill_id INT NOT NULL,
  item_id INT NOT NULL,
  qty INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  line_total DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (bill_id) REFERENCES bills(id),
  FOREIGN KEY (item_id) REFERENCES items(id)
);

-- Login Logs
CREATE TABLE IF NOT EXISTS login_logs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status ENUM('SUCCESS','FAIL') NOT NULL
);

-- Example stored procedure (optional) to calculate bill totals
DELIMITER //
CREATE PROCEDURE CalculateBill(IN p_bill_id INT)
BEGIN
  DECLARE v_sub DECIMAL(10,2);
  SELECT COALESCE(SUM(line_total),0) INTO v_sub
  FROM bill_items WHERE bill_id = p_bill_id;

  UPDATE bills
  SET sub_total = v_sub,
      tax = ROUND(v_sub * 0.02, 2),        -- 2% tax example
      total = ROUND(v_sub - discount + ROUND(v_sub * 0.02,2), 2)
  WHERE id = p_bill_id;
END //
DELIMITER ;
```

> Set a secure admin user with a hashed password (e.g., BCrypt/Argon2) during bootstrap or via an admin page/SQL insert.

---

## ⚙️ Configuration

Create `src/main/resources/db.properties` (or use environment variables).
```properties
db.url=jdbc:mysql://localhost:3306/pahana_edu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=your_mysql_user
db.password=your_mysql_password
db.pool.size=10
```

`DBConnection` uses a Singleton to read this file and provide the JDBC `DataSource/Connection`.

---

## ▶️ Running Locally

1. **Prereqs**
   - JDK 17+
   - Maven 3.9+
   - MySQL 8.x (running)
   - Apache Tomcat 9/10

2. **Clone & Configure**
   ```bash
   git clone https://github.com/<your-username>/pahana-edu-billing.git
   cd pahana-edu-billing
   # Create database and run the SQL above
   # Update src/main/resources/db.properties
   ```

3. **Build**
   ```bash
   mvn clean package
   ```

4. **Deploy**
   - Copy `target/pahana-edu-billing.war` to Tomcat's `webapps/`
   - Start Tomcat and open: `http://localhost:8080/pahana-edu-billing`

> **IntelliJ/Eclipse**: Configure a Tomcat run configuration and set your artifact to deploy on startup.

---

## 🔐 Authentication Notes

- Use session-based login (e.g., `HttpSession` with `userId`/`role` attributes).
- Add a simple `AuthFilter` to protect routes under `/app/*` and redirect unauthenticated users to `/login`.
- Log outcomes to `login_logs` for auditing.

---

## 🚦 Endpoints (examples)

- `GET /` → Home / Dashboard
- `GET /login`, `POST /login`
- `GET /customers`, `POST /customers/create`, `POST /customers/update`, `POST /customers/delete`
- `GET /items`, `POST /items/create`, `POST /items/update`, `POST /items/delete`
- `GET /bills/create`, `POST /bills/add-item`, `POST /bills/checkout`
- `GET /reports?month=YYYY-MM`

> Map these via `web.xml` or `@WebServlet` (if annotations are used).

---

## ✅ Testing

- **Unit Tests:** JUnit for service/DAO layers (mock DB where possible).
- **Integration:** Spin up a test schema (or H2 in MySQL mode) for DAO tests.
- **UI/Flow:** Manual smoke tests for critical paths (login → add item → create bill → report).

---

## 🗺️ Roadmap

- [ ] Role-based authorization (ADMIN/STAFF)
- [ ] Pagination & server-side search
- [ ] Export reports (PDF/CSV)
- [ ] REST endpoints for decoupled frontends
- [ ] Docker compose (MySQL + Tomcat)
- [ ] i18n + accessibility

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch: `git checkout -b feat/xyz`
3. Commit changes: `git commit -m "feat: add xyz"`
4. Push: `git push origin feat/xyz`
5. Open a Pull Request

---

## 📸 Screenshots (placeholders)

> Add your screenshots inside `/docs/screenshots/` and update the links below.

- Login  
  `docs/screenshots/login.png`
- Dashboard  
  `docs/screenshots/dashboard.png`
- Create Bill  
  `docs/screenshots/create-bill.png`
- Monthly Report  
  `docs/screenshots/report.png`

---

## 📝 License

This project is released under the **MIT License**. See [`LICENSE`](LICENSE) for details.

---

## 🙌 Acknowledgements

- Designed for educational use to demonstrate **Servlet/JSP MVC** with **MySQL**.
- Thanks to contributors, reviewers, and mentors.
