package project.Databases;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {
    private static final String DB_URL = "jdbc:sqlite:My.db";
    private static Connection connection;
    private static boolean schemaInitialized = false;
    private static Connect instance;
    
    // Private constructor to prevent direct instantiation
    private Connect() {}
    
    // Singleton pattern to get the instance
    public static synchronized Connect getInstance() {
        if (instance == null) {
            instance = new Connect();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            boolean isNewDatabase = !databaseExists();
            
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Connected to SQLite database: My.db");
                
                // Initialize schema only if this is a new database or schema hasn't been initialized
                if (isNewDatabase && !schemaInitialized) {
                    initializeSchema();
                    schemaInitialized = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        return connection;
    }
    
    /*** Check if the database file exists*/
    private boolean databaseExists() {
        File dbFile = new File("My.db");
        return dbFile.exists() && dbFile.length() > 0;
    }
    
    /*** Initialize the database schema and seed data*/
    private void initializeSchema() {
        try (Statement stmt = connection.createStatement()) {
            // Create admins table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                "email TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "age INTEGER NOT NULL," +
                "phone_number TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // Create members table
            stmt.execute("CREATE TABLE IF NOT EXISTS members (" +
                "email TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "age INTEGER NOT NULL," +
                "phone_number TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // Create books table
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL UNIQUE," +
                "author TEXT NOT NULL," +
                "total_copies INTEGER NOT NULL," +
                "available_copies INTEGER NOT NULL," +
                "borrow_count INTEGER DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // Create borrowed_books table
            stmt.execute("CREATE TABLE IF NOT EXISTS borrowed_books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "member_email TEXT NOT NULL," +
                "book_id INTEGER NOT NULL," +
                "borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "return_date TIMESTAMP NULL," +
                "is_returned BOOLEAN DEFAULT FALSE," +
                "FOREIGN KEY (member_email) REFERENCES members(email) ON DELETE CASCADE," +
                "FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE," +
                "UNIQUE(member_email, book_id, is_returned)" +
                ")");
            
            // Create indexes for better performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_borrowed_books_member ON borrowed_books(member_email)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_borrowed_books_book ON borrowed_books(book_id)");
            
            // Insert default admin accounts
            stmt.execute("INSERT OR IGNORE INTO admins (email, name, password, age, phone_number) VALUES" +
                "('admin@library.com', 'Admin', 'admin123', 30, '09000000000')," +
                "('system@library.com', 'System', 'sys123', 25, '09111111111')");
            
            // Insert default members
            stmt.execute("INSERT OR IGNORE INTO members (email, name, password, age, phone_number) VALUES" +
                "('kyle@gmail.com', 'Kyle', '1111', 20, '09123456789')," +
                "('allen@gmail.com', 'Allen', '1111', 21, '09234567890')," +
                "('rjay@gmail.com', 'Rjay', '1111', 22, '09345678901')," +
                "('jansean@gmail.com', 'Jansean', '1111', 23, '09456789012')," +
                "('kelly@gmail.com', 'Kelly', '1111', 21, '09234567890')," +
                "('colin@gmail.com', 'Colin', '1111', 22, '09345678901')," +
                "('kayezel@gmail.com', 'Kayezel', '1111', 23, '09456789012')");
            
            // Insert default books
            stmt.execute("INSERT OR IGNORE INTO books (title, author, total_copies, available_copies, borrow_count) VALUES" +
                "('To Kill a Mockingbird', 'Harper Lee', 5, 5, 0)," +
                "('Pride and Prejudice', 'Jane Austen', 5, 5, 0)," +
                "('Crime and Punishment', 'Fyodor Dostoevsky', 5, 5, 0)," +
                "('1984', 'George Orwell', 5, 5, 15)," +
                "('The Lord of the Rings', 'J.R.R. Tolkien', 5, 5, 0)," +
                "('The Great Gatsby', 'F. Scott Fitzgerald', 5, 5, 0)," +
                "('Noli Me Tangere', 'Jose Rizal', 5, 5, 0)");
            
            System.out.println("Database schema initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
        }
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null; // Allow garbage collection
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
