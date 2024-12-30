package uas.pam.budgetnotes2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChartActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart)

        pieChart = findViewById(R.id.pieChart)
        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

        loadChartData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadChartData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getAll()

            val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            val expenseAmount = transactions.filter { it.amount < 0 }.sumOf { Math.abs(it.amount) }
            val totalBalance = budgetAmount - expenseAmount

            val pieEntries = listOf(
                PieEntry((expenseAmount / budgetAmount).toFloat(), "Pengeluaran"),
                PieEntry((totalBalance / budgetAmount).toFloat(), "Sisa Saldo")
            )

            val pieDataSet = PieDataSet(pieEntries, "Grafik Transaksi")
            pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
            pieDataSet.valueTextSize = 16f
            pieDataSet.valueTextColor = android.graphics.Color.BLACK
            pieDataSet.valueFormatter = PercentFormatter(pieChart)

            val pieData = PieData(pieDataSet)

            withContext(Dispatchers.Main) {
                pieChart.data = pieData
                pieChart.description.isEnabled = false
                pieChart.centerText = "Pemasukan: Rp$budgetAmount"
                pieChart.isDrawHoleEnabled = true
                pieChart.setUsePercentValues(true)
                pieChart.animateY(1000)
                pieChart.invalidate()
            }
        }
    }
}