package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

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

        val email = findViewById<EditText>(R.id.etLoginEmail)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        val db = SovoraDatabase(this)

        btnLogin.setOnClickListener {

            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val userId = db.loginUser(userEmail, userPassword)

            if (userId != -1) {

                SessionManager(this).saveUser(userId)

                Toast.makeText(
                    this,
                    "Login successful",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this, Home::class.java))
                finish()

            } else {

                Toast.makeText(
                    this,
                    "Invalid email or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}