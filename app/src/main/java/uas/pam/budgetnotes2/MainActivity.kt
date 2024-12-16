package uas.pam.budgetnotes2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var transaction: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val addButton = findViewById<FloatingActionButton>(R.id.addButton)

        transaction = arrayListOf()

        transactionAdapter = TransactionAdapter(transaction)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        findViewById<RecyclerView>(R.id.recycleview).apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        addButton.setOnClickListener{
           val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchAll() {
        lifecycleScope.launch {
            transaction = db.transactionDao().getAll()
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transaction)
            }
        }
    }

    private fun updateDashboard(){
        val totalAmount = transaction.map { it.amount }.sum()
        val budgetAmount = transaction.filter { it.amount > 0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        findViewById<TextView>(R.id.balance).text = "Rp%.2f".format(totalAmount)
        findViewById<TextView>(R.id.budget).text = "Rp%.2f".format(budgetAmount)
        findViewById<TextView>(R.id.expense).text = "Rp%.2f".format(expenseAmount)
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}