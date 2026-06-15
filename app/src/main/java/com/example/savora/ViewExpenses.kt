package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ViewExpenses : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private lateinit var tvExpenses: TextView
    private lateinit var etSearch: EditText

    private var userId = -1
    private var allExpenses = ArrayList<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_expenses)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = SovoraDatabase(this)
        userId = SessionManager(this).getUserId()

        if (userId == -1) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        tvExpenses = findViewById(R.id.tvExpensesList)
        etSearch = findViewById(R.id.etSearchExpense)

        loadExpenses()
    }

    private fun loadExpenses() {
        allExpenses = db.getAllExpenses(userId)
        displayExpenses(allExpenses)
    }

    fun search_expenses(view: View) {
        val keyword = etSearch.text.toString().trim().lowercase()

        if (keyword.isEmpty()) {
            displayExpenses(allExpenses)
            return
        }

        val filtered = ArrayList<Expense>()

        for (expense in allExpenses) {
            if (
                expense.category.lowercase().contains(keyword) ||
                expense.description.lowercase().contains(keyword) ||
                expense.date.lowercase().contains(keyword) ||
                expense.amount.toString().contains(keyword)
            ) {
                filtered.add(expense)
            }
        }

        displayExpenses(filtered)
    }

    fun clear_search(view: View) {
        etSearch.text.clear()
        displayExpenses(allExpenses)
    }

    private fun displayExpenses(expensesList: ArrayList<Expense>) {
        if (expensesList.isEmpty()) {
            tvExpenses.text = "No expenses found..."
            return
        }

        val sb = StringBuilder()

        for (expense in expensesList) {
            sb.append("📅 Date: ${expense.date}\n")
            sb.append("⏰ Start: ${expense.startTime}\n")
            sb.append("⏰ End: ${expense.endTime}\n")
            sb.append("📝 Description: ${expense.description}\n")
            sb.append("🏷️ Category: ${expense.category}\n")
            sb.append("💰 Amount: R${"%.2f".format(expense.amount)}\n")
            sb.append("\n────────────────────\n\n")
        }

        tvExpenses.text = sb.toString()
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}