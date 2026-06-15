package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private var userId = -1

    private lateinit var tvHello: TextView
    private lateinit var etTotalAmount: EditText
    private lateinit var tvRemainingBalance: TextView
    private lateinit var tvTopCategory: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvSmartTip: TextView
    private lateinit var tvBudgetUsed: TextView
    private lateinit var tvExpenseCount: TextView
    private lateinit var tvHealthScore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        db = SovoraDatabase(this)
        userId = SessionManager(this).getUserId()

        if (userId == -1) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvHello = findViewById(R.id.tvHello)
        etTotalAmount = findViewById(R.id.etTotalAmount)
        tvRemainingBalance = findViewById(R.id.tvRemainingBalance)
        tvTopCategory = findViewById(R.id.tvTopCategory)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvSmartTip = findViewById(R.id.tvSmartTip)
        tvBudgetUsed = findViewById(R.id.tvBudgetUsed)
        tvExpenseCount = findViewById(R.id.tvExpenseCount)
        tvHealthScore = findViewById(R.id.tvHealthScore)

        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        if (::tvRemainingBalance.isInitialized) {
            loadDashboard()
        }
    }

    private fun loadDashboard() {
        val prefs = getSharedPreferences("BalancePrefs", MODE_PRIVATE)

        val firstName = db.getUserFirstName(userId)
        val startingBalance = prefs.getFloat("balance_$userId", 0f).toDouble()
        val totalSpent = db.getTotalSpent(userId)
        val remaining = startingBalance - totalSpent
        val topCategory = db.getTopCategory(userId)
        val expenseCount = db.getExpenseCount(userId)
        val budget = db.getBudget(userId)

        tvHello.text = "Hello, $firstName"

        if (startingBalance > 0) {
            etTotalAmount.setText("%.2f".format(startingBalance))
        }

        tvRemainingBalance.text = "Remaining Balance: R%.2f".format(remaining)
        tvTopCategory.text = "Top Category: $topCategory"
        tvTotalSpent.text = "Total Spent: R%.2f".format(totalSpent)
        tvExpenseCount.text = "Expenses Logged: $expenseCount"

        if (budget != null) {
            val minBudget = budget.first
            val maxBudget = budget.second

            val percentageUsed = if (maxBudget > 0) {
                ((totalSpent / maxBudget) * 100).toInt()
            } else {
                0
            }

            val healthScore = calculateHealthScore(totalSpent, minBudget, maxBudget)

            tvBudgetUsed.text = "Budget Used: $percentageUsed%"
            tvHealthScore.text = "Financial Health Score: $healthScore/100"

            tvSmartTip.text = when {
                totalSpent > maxBudget ->
                    "💡 Tip: You are over your maximum budget. Try reducing unnecessary spending."

                percentageUsed >= 80 ->
                    "💡 Tip: You are close to your maximum budget. Spend carefully."

                totalSpent < minBudget ->
                    "💡 Tip: You are below your minimum goal. You are saving well."

                else ->
                    "💡 Tip: You are within your budget range. Keep it up!"
            }

        } else {
            tvBudgetUsed.text = "Budget Used: 0%"
            tvHealthScore.text = "Financial Health Score: Set budget first"
            tvSmartTip.text = "💡 Tip: Set your budget to receive smart tips."
        }
    }

    private fun calculateHealthScore(
        totalSpent: Double,
        minBudget: Double,
        maxBudget: Double
    ): Int {
        if (maxBudget <= 0) return 0

        return when {
            totalSpent == 0.0 -> 100

            totalSpent < minBudget -> 95

            totalSpent in minBudget..maxBudget -> {
                val usedPercent = (totalSpent / maxBudget) * 100

                when {
                    usedPercent <= 50 -> 100
                    usedPercent <= 70 -> 90
                    usedPercent <= 85 -> 80
                    else -> 70
                }
            }

            else -> {
                val overPercent = ((totalSpent - maxBudget) / maxBudget) * 100

                when {
                    overPercent <= 10 -> 60
                    overPercent <= 25 -> 45
                    overPercent <= 50 -> 30
                    else -> 10
                }
            }
        }
    }

    fun save_balance(view: View) {
        val amount = etTotalAmount.text.toString().trim().toFloatOrNull()

        if (amount == null) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        getSharedPreferences("BalancePrefs", MODE_PRIVATE)
            .edit()
            .putFloat("balance_$userId", amount)
            .apply()

        Toast.makeText(this, "Balance Saved", Toast.LENGTH_SHORT).show()
        loadDashboard()
    }

    fun add_category(view: View) {
        startActivity(Intent(this, AddCategoryActivity::class.java))
    }

    fun add_expense(view: View) {
        startActivity(Intent(this, AddCategoryExpense::class.java))
    }

    fun view_expenses(view: View) {
        startActivity(Intent(this, ViewExpenses::class.java))
    }

    fun navigate_settings(view: View) {
        startActivity(Intent(this, BudgetSettingsActivity::class.java))
    }

    fun navigate_transactions(view: View) {
        startActivity(Intent(this, Transaction::class.java))
    }

    fun view_graph(view: View) {
        startActivity(Intent(this, GraphActivity::class.java))
    }

    fun open_budget_status(view: View) {
        startActivity(Intent(this, BudgetStatusActivity::class.java))
    }

    fun logout(view: View) {
        SessionManager(this).logout()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}