package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BudgetSettingsActivity : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget_settings)

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

        val etMin = findViewById<EditText>(R.id.etMinBudget)
        val etMax = findViewById<EditText>(R.id.etMaxBudget)
        val btnSave = findViewById<Button>(R.id.btnSaveBudget)

        // Load previously saved budget
        db.getBudget(userId)?.let { budget ->
            etMin.setText(budget.first.toString())
            etMax.setText(budget.second.toString())
        }

        btnSave.setOnClickListener {

            val min = etMin.text.toString().trim().toDoubleOrNull()
            val max = etMax.text.toString().trim().toDoubleOrNull()

            if (min == null || max == null) {
                Toast.makeText(
                    this,
                    "Enter valid amounts",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (min > max) {
                Toast.makeText(
                    this,
                    "Minimum budget cannot exceed maximum budget",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val success = db.saveBudget(min, max, userId)

            if (success) {

                Toast.makeText(
                    this,
                    "Budget saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Failed to save budget",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}