package com.example.savora

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SovoraDatabase(context: Context) :
    SQLiteOpenHelper(context, "savora_db", null, 6) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT,
                lastName TEXT,
                email TEXT UNIQUE,
                password TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                user_id INTEGER,
                UNIQUE(name, user_id),
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE budget (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                min_budget REAL,
                max_budget REAL,
                user_id INTEGER,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                start_time TEXT,
                end_time TEXT,
                description TEXT,
                amount REAL,
                category_id INTEGER,
                user_id INTEGER,
                FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS budget")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys=ON")
    }

    fun insertUser(firstName: String, lastName: String, email: String, password: String): Boolean {
        val values = ContentValues()
        values.put("firstName", firstName)
        values.put("lastName", lastName)
        values.put("email", email)
        values.put("password", password)
        return writableDatabase.insert("users", null, values) != -1L
    }

    fun loginUser(email: String, password: String): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM users WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )

        var userId = -1
        if (cursor.moveToFirst()) userId = cursor.getInt(0)

        cursor.close()
        return userId
    }

    fun getUserFirstName(userId: Int): String {
        val cursor = readableDatabase.rawQuery(
            "SELECT firstName FROM users WHERE id = ?",
            arrayOf(userId.toString())
        )

        var name = "User"
        if (cursor.moveToFirst()) name = cursor.getString(0)

        cursor.close()
        return name
    }

    fun insertCategory(name: String, userId: Int): Boolean {
        val values = ContentValues()
        values.put("name", name)
        values.put("user_id", userId)
        return writableDatabase.insert("categories", null, values) != -1L
    }

    fun getCategories(userId: Int): List<String> {
        val list = ArrayList<String>()

        val cursor = readableDatabase.rawQuery(
            "SELECT name FROM categories WHERE user_id = ? ORDER BY name ASC",
            arrayOf(userId.toString())
        )

        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }

        cursor.close()
        return list
    }

    fun insertExpense(
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        amount: Double,
        categoryName: String,
        userId: Int
    ): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM categories WHERE name = ? AND user_id = ?",
            arrayOf(categoryName, userId.toString())
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }

        val categoryId = cursor.getInt(0)
        cursor.close()

        val values = ContentValues()
        values.put("date", date)
        values.put("start_time", startTime)
        values.put("end_time", endTime)
        values.put("description", description)
        values.put("amount", amount)
        values.put("category_id", categoryId)
        values.put("user_id", userId)

        return writableDatabase.insert("expenses", null, values) != -1L
    }

    fun getAllExpenses(userId: Int): ArrayList<Expense> {
        val list = ArrayList<Expense>()

        val query = """
            SELECT e.date, e.start_time, e.end_time, e.description, e.amount, c.name
            FROM expenses e
            LEFT JOIN categories c ON e.category_id = c.id
            WHERE e.user_id = ?
            ORDER BY e.date DESC
        """

        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))

        while (cursor.moveToNext()) {
            list.add(
                Expense(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getDouble(4),
                    cursor.getString(5)
                )
            )
        }

        cursor.close()
        return list
    }

    fun saveBudget(min: Double, max: Double, userId: Int): Boolean {
        val values = ContentValues()
        values.put("min_budget", min)
        values.put("max_budget", max)
        values.put("user_id", userId)
        return writableDatabase.insert("budget", null, values) != -1L
    }

    fun getBudget(userId: Int): Pair<Double, Double>? {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT min_budget, max_budget
            FROM budget
            WHERE user_id = ?
            ORDER BY id DESC
            LIMIT 1
            """,
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            val min = cursor.getDouble(0)
            val max = cursor.getDouble(1)
            cursor.close()
            return Pair(min, max)
        }

        cursor.close()
        return null
    }

    fun getTopCategory(userId: Int): String {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT c.name, SUM(e.amount) AS total
            FROM expenses e
            JOIN categories c ON e.category_id = c.id
            WHERE e.user_id = ?
            GROUP BY c.name
            ORDER BY total DESC
            LIMIT 1
            """,
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            val category = cursor.getString(0)
            cursor.close()
            return category
        }

        cursor.close()
        return "No data"
    }

    fun getTotalSpent(userId: Int): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE user_id = ?",
            arrayOf(userId.toString())
        )

        var total = 0.0
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        return total
    }

    fun getExpenseCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM expenses WHERE user_id = ?",
            arrayOf(userId.toString())
        )

        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)

        cursor.close()
        return count
    }
}

data class Expense(
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val amount: Double,
    val category: String
)