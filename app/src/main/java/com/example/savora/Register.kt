package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {

    private lateinit var db: SovoraDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        db = SovoraDatabase(this)

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
    }

    fun register_user(view: View) {

        val firstName = findViewById<EditText>(R.id.etFirstName)
            .text.toString().trim()

        val lastName = findViewById<EditText>(R.id.etLastName)
            .text.toString().trim()

        val email = findViewById<EditText>(R.id.etEmail)
            .text.toString().trim()

        val password = findViewById<EditText>(R.id.etPassword)
            .text.toString().trim()

        if (
            firstName.isEmpty() ||
            lastName.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Please fill all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val success = db.insertUser(
            firstName,
            lastName,
            email,
            password
        )

        if (success) {

            Toast.makeText(
                this,
                "Registered successfully. Please login.",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this, Login::class.java))
            finish()

        } else {

            Toast.makeText(
                this,
                "Registration failed. Email may already exist.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun GotoLogin(view: View) {
        startActivity(Intent(this, Login::class.java))
        finish()
    }
}