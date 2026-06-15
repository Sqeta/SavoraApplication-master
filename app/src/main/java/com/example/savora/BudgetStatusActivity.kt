package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BudgetStatusActivity : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private lateinit var tvStatus: TextView

    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_status)

        db = SovoraDatabase(this)

        userId = SessionManager(this).getUserId()

        if (userId == -1) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        tvStatus = findViewById(R.id.tvStatus)

        showStatus()
    }

    private fun showStatus() {

        val budget = db.getBudget(userId)
        val expenses = db.getAllExpenses(userId)

        if (budget == null) {
            tvStatus.text = "No budget set yet"
            return
        }

        var total = 0.0

        for (e in expenses) {
            total += e.amount
        }

        val min = budget.first
        val max = budget.second

        val badge = getBadge(total, min, max)

        val message = """
📊 BUDGET STATUS

💰 Total Spent: R${"%.2f".format(total)}

📉 Minimum Goal: R${"%.2f".format(min)}

📈 Maximum Goal: R${"%.2f".format(max)}

🏆 Reward:
$badge
        """.trimIndent()

        tvStatus.text = message
    }

    // GAMIFICATION
    private fun getBadge(
        total: Double,
        min: Double,
        max: Double
    ): String {

        return when {

            total > max ->
                "🔴 BRONZE BADGE\nYou are over budget"

            total in min..max ->
                "🟢 GOLD BADGE\nPerfect budget control!"

            else ->
                "🟡 SILVER BADGE\nYou are below budget and saving well!"
        }
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}