package com.example.savora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    private var categoryList = ArrayList<String>()
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_category)

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

        val etCategory = findViewById<EditText>(R.id.etCategory)
        val btnSave = findViewById<Button>(R.id.btnSaveCategory)

        listView = findViewById(R.id.listCategories)

        loadCategories()

        btnSave.setOnClickListener {

            val name = etCategory.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(
                    this,
                    "Enter category name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // ✅ Correct parameter order
            val success = db.insertCategory(name, userId)

            if (success) {

                Toast.makeText(
                    this,
                    "Category added",
                    Toast.LENGTH_SHORT
                ).show()

                etCategory.text.clear()

                loadCategories()

            } else {

                Toast.makeText(
                    this,
                    "Category already exists",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadCategories() {

        categoryList = ArrayList(db.getCategories(userId))

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            categoryList
        )

        listView.adapter = adapter
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}