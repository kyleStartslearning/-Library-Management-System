-- Create admins table
CREATE TABLE IF NOT EXISTS admins (
    email TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    age INTEGER NOT NULL,
    phone_number TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create members table  
CREATE TABLE IF NOT EXISTS members (
    email TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    age INTEGER NOT NULL,
    phone_number TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create books table with flexible user tracking
CREATE TABLE IF NOT EXISTS books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL UNIQUE,
    author TEXT NOT NULL,
    total_copies INTEGER NOT NULL,
    available_copies INTEGER NOT NULL,
    borrow_count INTEGER DEFAULT 0,
    added_by_email TEXT NOT NULL,  -- Can be admin or member email
    added_by_type TEXT NOT NULL CHECK (added_by_type IN ('admin', 'member')),  -- Track user type
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by_email TEXT,
    updated_by_type TEXT CHECK (updated_by_type IN ('admin', 'member'))
);

-- Create borrowed_books table (unchanged)
CREATE TABLE IF NOT EXISTS borrowed_books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_email TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    return_date TIMESTAMP NULL,
    is_returned BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (member_email) REFERENCES members(email) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE(member_email, book_id, is_returned) -- Prevent duplicate active borrows
);

-- Insert default admin accounts
INSERT OR IGNORE INTO admins (email, name, password, age, phone_number) VALUES 
('admin@library.com', 'Admin', 'admin123', 30, '09000000000'),
('system@library.com', 'System', 'sys123', 25, '09111111111');

-- Insert default members
INSERT OR IGNORE INTO members (email, name, password, age, phone_number) VALUES 
('kyle@gmail.com', 'Kyle', '1111', 20, '09123456789'),
('allen@gmail.com', 'Allen', '1111', 21, '09234567890'),
('rjay@gmail.com', 'Rjay', '1111', 22, '09345678901'),
('jansean@gmail.com', 'Jansean', '1111', 23, '09456789012'),
('kelly@gmail.com', 'Kelly', '1111', 21, '09234567890'),
('colin@gmail.com', 'Colin', '1111', 22, '09345678901'),
('kayezel@gmail.com', 'Kayezel', '1111', 23, '09456789012');

-- Insert default books with system as the creator
INSERT OR IGNORE INTO books (title, author, total_copies, available_copies, borrow_count, added_by_email, added_by_type) VALUES 
('To Kill a Mockingbird', 'Harper Lee', 5, 5, 0, 'system@library.com', 'admin'),
('Pride and Prejudice', 'Jane Austen', 5, 5, 0, 'system@library.com', 'admin'),
('Crime and Punishment', 'Fyodor Dostoevsky', 5, 5, 0, 'system@library.com', 'admin'),
('1984', 'George Orwell', 5, 5, 15, 'system@library.com', 'admin'),
('The Lord of the Rings', 'J.R.R. Tolkien', 5, 5, 0, 'system@library.com', 'admin'),
('The Great Gatsby', 'F. Scott Fitzgerald', 5, 5, 0, 'system@library.com', 'admin'),
('Noli Me Tangere', 'Jose Rizal', 5, 5, 0, 'system@library.com', 'admin'),
('El Filibusterismo', 'Jose Rizal', 5, 5, 0, 'system@library.com', 'admin'),
('One Piece', 'Eiichiro Oda', 5, 5, 0, 'system@library.com', 'admin'),
('Bleach', 'Tite Kubo', 5, 5, 0, 'system@library.com', 'admin'),
('Fairy Tail', 'Hiro Mashima', 5, 5, 0, 'system@library.com', 'admin'),
('DanMachi', 'Fujino Omori', 5, 5, 0, 'system@library.com', 'admin'),
('One Punch Man', 'ONE', 5, 5, 0, 'system@library.com', 'admin'),
('Jujutsu Kaisen', 'Gege Akutami', 5, 5, 0, 'system@library.com', 'admin');

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_borrowed_books_member ON borrowed_books(member_email);
CREATE INDEX IF NOT EXISTS idx_borrowed_books_book ON borrowed_books(book_id);
CREATE INDEX IF NOT EXISTS idx_borrowed_books_status ON borrowed_books(is_returned);
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_added_by ON books(added_by_email);
CREATE INDEX IF NOT EXISTS idx_books_user_type ON books(added_by_type);

-- Updated views with flexible user tracking
CREATE VIEW IF NOT EXISTS books_with_user_info AS
SELECT 
    b.id,
    b.title,
    b.author,
    b.total_copies,
    b.available_copies,
    b.borrow_count,
    b.created_at,
    b.added_by_email,
    b.added_by_type,
    CASE 
        WHEN b.added_by_type = 'admin' THEN 
            (SELECT name FROM admins WHERE email = b.added_by_email)
        WHEN b.added_by_type = 'member' THEN 
            (SELECT name FROM members WHERE email = b.added_by_email)
        ELSE 'Unknown'
    END AS added_by_name,
    b.updated_at,
    b.updated_by_email,
    b.updated_by_type,
    CASE 
        WHEN b.updated_by_type = 'admin' THEN 
            (SELECT name FROM admins WHERE email = b.updated_by_email)
        WHEN b.updated_by_type = 'member' THEN 
            (SELECT name FROM members WHERE email = b.updated_by_email)
        ELSE NULL
    END AS updated_by_name
FROM books b;

CREATE VIEW IF NOT EXISTS member_borrowed_books AS
SELECT 
    m.name AS member_name,
    m.email AS member_email,
    b.title AS book_title,
    b.author AS book_author,
    bb.borrow_date,
    bb.return_date,
    bb.is_returned
FROM borrowed_books bb
JOIN members m ON bb.member_email = m.email
JOIN books b ON bb.book_id = b.id;

CREATE VIEW IF NOT EXISTS library_statistics AS
SELECT 
    (SELECT COUNT(*) FROM members) AS total_members,
    (SELECT COUNT(*) FROM books) AS total_books,
    (SELECT SUM(total_copies) FROM books) AS total_book_copies,
    (SELECT SUM(available_copies) FROM books) AS available_copies,
    (SELECT COUNT(*) FROM borrowed_books WHERE is_returned = FALSE) AS currently_borrowed,
    (SELECT SUM(borrow_count) FROM books) AS total_borrows_ever,
    (SELECT COUNT(*) FROM books WHERE added_by_type = 'admin') AS books_added_by_admins,
    (SELECT COUNT(*) FROM books WHERE added_by_type = 'member') AS books_added_by_members;

-- User activity tracking
CREATE VIEW IF NOT EXISTS user_book_contributions AS
SELECT 
    added_by_email,
    added_by_type,
    CASE 
        WHEN added_by_type = 'admin' THEN 
            (SELECT name FROM admins WHERE email = added_by_email)
        WHEN added_by_type = 'member' THEN 
            (SELECT name FROM members WHERE email = added_by_email)
    END AS user_name,
    COUNT(*) AS books_contributed,
    MIN(created_at) AS first_contribution,
    MAX(created_at) AS last_contribution
FROM books
GROUP BY added_by_email, added_by_type
ORDER BY books_contributed DESC;    