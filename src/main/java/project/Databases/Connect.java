package project.Databases;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

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
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Connected to SQLite database: My.db");
                
                // Always initialize/update schema (for development)
                if (!schemaInitialized) {
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
            
            // Create books table with flexible user tracking
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL UNIQUE," +
                "author TEXT NOT NULL," +
                "total_copies INTEGER NOT NULL," +
                "available_copies INTEGER NOT NULL," +
                "borrow_count INTEGER DEFAULT 0," +
                "added_by_email TEXT NOT NULL," +
                "added_by_type TEXT NOT NULL CHECK (added_by_type IN ('admin', 'member'))," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_by_email TEXT," +
                "updated_by_type TEXT CHECK (updated_by_type IN ('admin', 'member'))" +
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
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_borrowed_books_status ON borrowed_books(is_returned)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_author ON books(author)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_added_by ON books(added_by_email)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_user_type ON books(added_by_type)");
            
            // Insert default admin accounts
            stmt.execute("INSERT OR IGNORE INTO admins (email, name, password, age, phone_number) VALUES " +
                "('admin', 'Admin', 'admin123', 30, '09000000000'), " +
                "('system@library.com', 'System', 'sys123', 25, '09111111111')");
            
            // Insert default members
            stmt.execute("INSERT OR IGNORE INTO members (email, name, password, age, phone_number) VALUES " +
                "('kyle@gmail.com', 'Kyle', '1111', 20, '09123456789'), " +
                "('allen@gmail.com', 'Allen', '1111', 21, '09234567890'), " +
                "('rjay@gmail.com', 'Rjay', '1111', 22, '09345678901'), " +
                "('jansean@gmail.com', 'Jansean', '1111', 23, '09456789012'), " +
                "('kelly@gmail.com', 'Kelly', '1111', 21, '09234567890'), " +
                "('colin@gmail.com', 'Colin', '1111', 22, '09345678901'), " +
                "('kayezel@gmail.com', 'Kayezel', '1111', 23, '09456789012')");
            
            // Insert default books with system as the creator
            stmt.execute("INSERT OR IGNORE INTO books (title, author, total_copies, available_copies, borrow_count, added_by_email, added_by_type) VALUES " +
                "('To Kill a Mockingbird', 'Harper Lee', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('Pride and Prejudice', 'Jane Austen', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('Crime and Punishment', 'Fyodor Dostoevsky', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('1984', 'George Orwell', 5, 5, 15, 'system@library.com', 'admin'), " +
                "('The Lord of the Rings', 'J.R.R. Tolkien', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('The Great Gatsby', 'F. Scott Fitzgerald', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('Noli Me Tangere', 'Jose Rizal', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('El Filibusterismo', 'Jose Rizal', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('One Piece', 'Eiichiro Oda', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('Bleach', 'Tite Kubo', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('Fairy Tail', 'Hiro Mashima', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('DanMachi', 'Fujino Omori', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('One Punch Man', 'ONE', 5, 5, 0, 'system@library.com', 'admin'), " +
                "('Jujutsu Kaisen', 'Gege Akutami', 5, 5, 0, 'system@library.com', 'admin')");
            
            // Create views with flexible user tracking
            stmt.execute("CREATE VIEW IF NOT EXISTS books_with_user_info AS " +
                "SELECT " +
                "b.id, " +
                "b.title, " +
                "b.author, " +
                "b.total_copies, " +
                "b.available_copies, " +
                "b.borrow_count, " +
                "b.created_at, " +
                "b.added_by_email, " +
                "b.added_by_type, " +
                "CASE " +
                "    WHEN b.added_by_type = 'admin' THEN " +
                "        (SELECT name FROM admins WHERE email = b.added_by_email) " +
                "    WHEN b.added_by_type = 'member' THEN " +
                "        (SELECT name FROM members WHERE email = b.added_by_email) " +
                "    ELSE 'Unknown' " +
                "END AS added_by_name, " +
                "b.updated_at, " +
                "b.updated_by_email, " +
                "b.updated_by_type, " +
                "CASE " +
                "    WHEN b.updated_by_type = 'admin' THEN " +
                "        (SELECT name FROM admins WHERE email = b.updated_by_email) " +
                "    WHEN b.updated_by_type = 'member' THEN " +
                "        (SELECT name FROM members WHERE email = b.updated_by_email) " +
                "    ELSE NULL " +
                "END AS updated_by_name " +
                "FROM books b");
            
            stmt.execute("CREATE VIEW IF NOT EXISTS member_borrowed_books AS " +
                "SELECT " +
                "m.name AS member_name, " +
                "m.email AS member_email, " +
                "b.title AS book_title, " +
                "b.author AS book_author, " +
                "bb.borrow_date, " +
                "bb.return_date, " +
                "bb.is_returned " +
                "FROM borrowed_books bb " +
                "JOIN members m ON bb.member_email = m.email " +
                "JOIN books b ON bb.book_id = b.id");
            
            stmt.execute("CREATE VIEW IF NOT EXISTS library_statistics AS " +
                "SELECT " +
                "(SELECT COUNT(*) FROM members) AS total_members, " +
                "(SELECT COUNT(*) FROM books) AS total_books, " +
                "(SELECT SUM(total_copies) FROM books) AS total_book_copies, " +
                "(SELECT SUM(available_copies) FROM books) AS available_copies, " +
                "(SELECT COUNT(*) FROM borrowed_books WHERE is_returned = FALSE) AS currently_borrowed, " +
                "(SELECT SUM(borrow_count) FROM books) AS total_borrows_ever, " +
                "(SELECT COUNT(*) FROM books WHERE added_by_type = 'admin') AS books_added_by_admins, " +
                "(SELECT COUNT(*) FROM books WHERE added_by_type = 'member') AS books_added_by_members");
            
            stmt.execute("CREATE VIEW IF NOT EXISTS user_book_contributions AS " +
                "SELECT " +
                "added_by_email, " +
                "added_by_type, " +
                "CASE " +
                "    WHEN added_by_type = 'admin' THEN " +
                "        (SELECT name FROM admins WHERE email = added_by_email) " +
                "    WHEN added_by_type = 'member' THEN " +
                "        (SELECT name FROM members WHERE email = added_by_email) " +
                "END AS user_name, " +
                "COUNT(*) AS books_contributed, " +
                "MIN(created_at) AS first_contribution, " +
                "MAX(created_at) AS last_contribution " +
                "FROM books " +
                "GROUP BY added_by_email, added_by_type " +
                "ORDER BY books_contributed DESC");
            
           
            stmt.execute("INSERT OR IGNORE INTO borrowed_books (member_email, book_id, borrow_date, return_date, is_returned) VALUES " +
                "('kyle@gmail.com', 1, '2024-07-15 10:30:00', '2024-07-25 14:20:00', 1), " +
                "('allen@gmail.com', 3, '2024-07-18 09:15:00', '2024-07-28 16:45:00', 1), " +
                "('kelly@gmail.com', 5, '2024-07-20 11:00:00', '2024-07-30 13:30:00', 1), " +
                "('colin@gmail.com', 7, '2024-07-22 14:30:00', '2024-08-01 10:15:00', 1), " +
                "('rjay@gmail.com', 2, '2024-07-10 08:45:00', '2024-07-20 17:20:00', 1), " +
                "('jansean@gmail.com', 9, '2024-07-12 16:20:00', '2024-07-22 12:10:00', 1), " +
                "('kayezel@gmail.com', 11, '2024-07-14 13:45:00', '2024-07-24 15:30:00', 1), " +
                
                // Currently borrowed books (not returned yet)
                "('kyle@gmail.com', 14, '2024-07-28 10:30:00', NULL, 0), " + 
                "('allen@gmail.com', 12, '2024-07-30 14:15:00', NULL, 0), " +
                "('kelly@gmail.com', 8, '2024-08-01 09:20:00', NULL, 0), " +  
                "('rjay@gmail.com', 6, '2024-08-02 11:45:00', NULL, 0), " +  
                "('colin@gmail.com', 13, '2024-07-25 16:30:00', NULL, 0), " +
                
                // Some members borrowed multiple books over time
                "('kyle@gmail.com', 4, '2024-06-15 10:00:00', '2024-06-25 14:30:00', 1), " + 
                "('kyle@gmail.com', 10, '2024-06-28 12:15:00', '2024-07-08 16:45:00', 1), " +
                "('allen@gmail.com', 4, '2024-06-20 09:30:00', '2024-06-30 13:20:00', 1), " +
                "('kelly@gmail.com', 1, '2024-06-10 14:20:00', '2024-06-20 11:10:00', 1), " + 
                "('jansean@gmail.com', 4, '2024-07-05 15:45:00', '2024-07-15 10:30:00', 1)"); 

            // Update borrow counts for popular books
            stmt.execute("UPDATE books SET borrow_count = 3 WHERE id = 4"); // 1984 - very popular
            stmt.execute("UPDATE books SET borrow_count = 2 WHERE id = 1"); // To Kill a Mockingbird
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 2"); // Pride and Prejudice  
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 3"); // Crime and Punishment
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 5"); // The Lord of the Rings
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 6"); // The Great Gatsby
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 7"); // Noli Me Tangere
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 8"); // El Filibusterismo
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 9"); // One Piece
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 10"); // Bleach
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 11"); // Fairy Tail
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 12"); // DanMachi
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 13"); // One Punch Man
            stmt.execute("UPDATE books SET borrow_count = 1 WHERE id = 14"); // Jujutsu Kaisen

            // Update available copies for currently borrowed books
            stmt.execute("UPDATE books SET available_copies = 4 WHERE id IN (6, 8, 12, 13, 14)"); 

            // Add some member-contributed books to make it more realistic
            stmt.execute("INSERT OR IGNORE INTO books (title, author, total_copies, available_copies, borrow_count, added_by_email, added_by_type) VALUES " +
                "('The Alchemist', 'Paulo Coelho', 3, 3, 0, 'kyle@gmail.com', 'member'), " +
                "('Rich Dad Poor Dad', 'Robert Kiyosaki', 2, 2, 0, 'allen@gmail.com', 'member'), " +
                "('Atomic Habits', 'James Clear', 3, 2, 1, 'kelly@gmail.com', 'member'), " +
                "('Sapiens', 'Yuval Noah Harari', 2, 2, 0, 'rjay@gmail.com', 'member'), " +
                "('The 7 Habits', 'Stephen Covey', 2, 1, 1, 'colin@gmail.com', 'member')");
            
            // Add some borrowing history for member-contributed books
            stmt.execute("INSERT OR IGNORE INTO borrowed_books (member_email, book_id, borrow_date, return_date, is_returned) VALUES " +
                "('jansean@gmail.com', 17, '2024-07-20 10:30:00', '2024-07-30 14:20:00', 1), " + 
                "('kayezel@gmail.com', 19, '2024-07-25 11:15:00', NULL, 0)"); 

            System.out.println("Database schema initialized successfully with audit trail support");
            
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
    
    /**
     * Static utility method to get database connection quickly
     */
    public static Connection getDBConnection() {
        return Connect.getInstance().getConnection();
    }
    
    /**
     * Execute an update/insert/delete query with parameters
     * @param query SQL query with ? placeholders
     * @param params Parameters to bind to the query
     * @return true if operation was successful
     */
    public static boolean executeUpdate(String query, Object... params) {
        try (Connection connection = getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Database update error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute a query and process results with a lambda function
     * @param query SQL query with ? placeholders
     * @param processor Function to process the ResultSet
     * @param params Parameters to bind to the query
     * @return Result from the processor function
     */
    public static <T> T executeQuery(String query, Function<ResultSet, T> processor, Object... params) {
        try (Connection connection = getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return processor.apply(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Database query error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Execute a simple query that returns a single value
     * @param query SQL query
     * @param params Parameters
     * @return First column of first row as String, or null
     */
    public static String executeScalar(String query, Object... params) {
        return executeQuery(query, rs -> {
            try {
                return rs.next() ? rs.getString(1) : null;
            } catch (SQLException e) {
                return null;
            }
        }, params);
    }
    
    /**
     * Execute a query that returns a count
     * @param query SQL query that returns a count
     * @param params Parameters
     * @return Count as integer, or 0 if error
     */
    public static int executeCount(String query, Object... params) {
        return executeQuery(query, rs -> {
            try {
                return rs.next() ? rs.getInt(1) : 0;
            } catch (SQLException e) {
                return 0;
            }
        }, params);
    }
}
