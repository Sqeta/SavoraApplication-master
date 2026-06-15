package com.example.savora

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class GraphActivity : AppCompatActivity() {

    private lateinit var db: SovoraDatabase
    private lateinit var barChart: BarChart
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        db = SovoraDatabase(this)
        userId = SessionManager(this).getUserId()

        if (userId == -1) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        barChart = findViewById(R.id.barChart)

        setupChart()
        loadData()
    }

    private fun setupChart() {
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1500)
        barChart.setDrawValueAboveBar(true)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -30f

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.removeAllLimitLines()
    }

    private fun loadData() {
        val expenses = db.getAllExpenses(userId)

        if (expenses.isEmpty()) {
            Toast.makeText(this, "No expenses available for graph", Toast.LENGTH_SHORT).show()
            barChart.clear()
            return
        }

        val map = linkedMapOf<String, Float>()

        for (e in expenses) {
            map[e.category] = (map[e.category] ?: 0f) + e.amount.toFloat()
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        var index = 0f

        for ((category, total) in map) {
            entries.add(BarEntry(index, total))
            labels.add(category)
            index++
        }

        val dataSet = BarDataSet(entries, "Spending by Category")
        dataSet.color = Color.parseColor("#2E7D32")
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        barChart.data = data

        barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val position = value.toInt()
                return if (position >= 0 && position < labels.size) labels[position] else ""
            }
        }

        val budget = db.getBudget(userId)

        if (budget != null) {
            val minBudget = budget.first.toFloat()
            val maxBudget = budget.second.toFloat()

            val minLine = LimitLine(minBudget, "Min Goal: R${"%.2f".format(budget.first)}")
            minLine.lineColor = Color.parseColor("#FF9800")
            minLine.textColor = Color.parseColor("#FF9800")
            minLine.lineWidth = 2f
            minLine.textSize = 11f

            val maxLine = LimitLine(maxBudget, "Max Goal: R${"%.2f".format(budget.second)}")
            maxLine.lineColor = Color.RED
            maxLine.textColor = Color.RED
            maxLine.lineWidth = 2f
            maxLine.textSize = 11f

            barChart.axisLeft.removeAllLimitLines()
            barChart.axisLeft.addLimitLine(minLine)
            barChart.axisLeft.addLimitLine(maxLine)
        } else {
            Toast.makeText(this, "Set your budget first to show min/max goals", Toast.LENGTH_SHORT).show()
        }

        barChart.invalidate()
    }

    fun back_home(view: View) {
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}