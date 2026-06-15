package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Transaction : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private lateinit var tvResult: TextView
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        db = SovoraDatabase(this)
        userId = SessionManager(this).getUserId()

        if (userId == -1) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        val etStart = findViewById<EditText>(R.id.etStartDate)
        val etEnd = findViewById<EditText>(R.id.etEndDate)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateReport)
        tvResult = findViewById(R.id.tvReportResult)

        btnGenerate.setOnClickListener {
            val startDate = etStart.text.toString().trim()
            val endDate = etEnd.text.toString().trim()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                tvResult.text = "Please enter both dates"
                return@setOnClickListener
            }

            if (startDate > endDate) {
                tvResult.text = "Start date must be before end date"
                return@setOnClickListener
            }

            loadTransactions(startDate, endDate)
        }
    }

    private fun loadTransactions(startDate: String, endDate: String) {
        val dbRead = db.readableDatabase

        val query = """
            SELECT e.date, e.start_time, e.end_time, e.description, e.amount, c.name AS category
            FROM expenses e
            LEFT JOIN categories c ON e.category_id = c.id
            WHERE e.user_id = ? AND e.date BETWEEN ? AND ?
            ORDER BY e.date ASC
        """.trimIndent()

        val cursor = dbRead.rawQuery(
            query,
            arrayOf(userId.toString(), startDate, endDate)
        )

        if (cursor.count == 0) {
            tvResult.text = "No transactions found for selected dates"
            cursor.close()
            return
        }

        val sb = StringBuilder()
        var total = 0.0

        while (cursor.moveToNext()) {
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val start = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
            val end = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))

            total += amount

            sb.append("📅 Date: $date\n")
            sb.append("⏰ Start: $start\n")
            sb.append("⏰ End: $end\n")
            sb.append("📝 Desc: $desc\n")
            sb.append("🏷️ Category: $category\n")
            sb.append("💰 Amount: R${"%.2f".format(amount)}\n")
            sb.append("\n====================\n\n")
        }

        cursor.close()

        sb.append("TOTAL SPENT: R${"%.2f".format(total)}")

        tvResult.text = sb.toString()
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}