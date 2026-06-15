package com.example.savora

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddCategoryExpense : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private lateinit var spinner: Spinner

    private var categoryList = ArrayList<String>()
    private var imageUri: Uri? = null
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category_expense)

        db = SovoraDatabase(this)

        userId = SessionManager(this).getUserId()

        if (userId == -1) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        val etDate = findViewById<EditText>(R.id.etDate)
        val etStart = findViewById<EditText>(R.id.etStartTime)
        val etEnd = findViewById<EditText>(R.id.etEndTime)
        val etDesc = findViewById<EditText>(R.id.etDescription)
        val etAmount = findViewById<EditText>(R.id.etAmount)

        val btnSave = findViewById<Button>(R.id.btnSaveExpense)
        val btnPhoto = findViewById<Button>(R.id.btnAddPhoto)

        spinner = findViewById(R.id.spCategory)

        loadCategories()

        btnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        btnSave.setOnClickListener {

            val date = etDate.text.toString().trim()
            val start = etStart.text.toString().trim()
            val end = etEnd.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val category = spinner.selectedItem?.toString()

            if (date.isEmpty() ||
                desc.isEmpty() ||
                amountText.isEmpty() ||
                category.isNullOrEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Fill all required fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()

            if (amount == null) {
                Toast.makeText(
                    this,
                    "Enter a valid amount",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val success = db.insertExpense(
                date,
                start,
                end,
                desc,
                amount,
                category,
                userId
            )

            if (success) {

                Toast.makeText(
                    this,
                    "Expense saved",
                    Toast.LENGTH_SHORT
                ).show()

                clearFields()

            } else {

                Toast.makeText(
                    this,
                    "Error saving expense",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadCategories() {

        categoryList = ArrayList(db.getCategories(userId))

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryList
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = adapter
    }

    private fun clearFields() {
        findViewById<EditText>(R.id.etDate).text.clear()
        findViewById<EditText>(R.id.etStartTime).text.clear()
        findViewById<EditText>(R.id.etEndTime).text.clear()
        findViewById<EditText>(R.id.etDescription).text.clear()
        findViewById<EditText>(R.id.etAmount).text.clear()
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}