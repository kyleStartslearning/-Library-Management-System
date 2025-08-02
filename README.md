# 📚 Library Management System

A modern JavaFX-based Library Management System with SQLite database for efficient book and member management.

![Java](https://img.shields.io/badge/Java-11+-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17-orange.svg)
![SQLite](https://img.shields.io/badge/SQLite-3-green.svg)
![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)

## ✨ Features

### 📖 Book Management
- ➕ Add new books to the library
- 🔍 Search books by title or author
- 📊 View all books with availability status
- 🗑️ Remove books from inventory

### 👥 Member Management
- 👤 Register new library members
- 🔍 Search members by name or email
- 📋 View all registered members
- 🗑️ Remove member accounts

### 📚 Borrowing System
- 📖 Borrow available books
- 📅 Track borrowing dates
- 🔄 Return borrowed books
- 📊 View borrowing history

### 🔐 Authentication
- 🛡️ Secure admin and member login
- 👨‍💼 Role-based access control
- 📊 Admin dashboard with statistics

### 📊 Statistics & Reporting
- 📈 Total books and members count
- 📚 Available vs borrowed books
- 👥 Member activity tracking

## 🚀 Quick Start

### Prerequisites
- ☕ Java 11 or higher
- 📦 Maven 3.6+

### Installation & Running

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/library-management-system.git
cd library-management-system
```

2. **Run the application**
```bash
mvn clean javafx:run
```

That's it! The application will:
- ✅ Automatically create the SQLite database
- ✅ Set up all necessary tables
- ✅ Insert sample data
- ✅ Launch the login window

## 🔑 Default Login Credentials

### Admin Account
- **Email:** `admin@library.com`
- **Password:** `admin123`

### Member Account
- **Email:** `john.doe@email.com`
- **Password:** `password123`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/project/
│   │   ├── controllers/          # JavaFX Controllers
│   │   │   ├── AdminControllers.java
│   │   │   ├── LoginController.java
│   │   │   └── MemberController.java
│   │   ├── Databases/           # Database Models & Connection
│   │   │   ├── Connect.java
│   │   │   ├── Book.java
│   │   │   ├── Admin.java
│   │   │   └── LibraryMember.java
│   │   ├── Utilities/           # Utility Classes
│   │   │   ├── UIUtil.java
│   │   │   ├── AlertMsg.java
│   │   │   ├── SwitchSceneUtil.java
│   │   │   └── AdminUtil/
│   │   └── Main.java            # Application Entry Point
│   └── resources/project/
│       ├── FXML/               # JavaFX FXML Files
│       │   ├── LogIn.fxml
│       │   ├── admin.fxml
│       │   └── member.fxml
│       └── CSS/                # Stylesheets
│           ├── LogIn.css
│           ├── admin.css
│           └── member.css
└── module-info.java           # Java Module Configuration
```

## 🗄️ Database

- **Type:** SQLite (embedded database)
- **File:** `My.db` (automatically created on first run)
- **Schema:** Auto-initialized with tables for books, members, admins, and borrowing records

### Database Tables
- `books` - Book inventory
- `members` - Library members
- `admins` - Administrator accounts
- `borrowed_books` - Borrowing transactions

## 🧪 Testing

Run the console test utility to verify database operations:
```bash
mvn exec:java -Dexec.mainClass="project.ConsoleTest"
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

⭐ **Star this repository if you found it helpful!**