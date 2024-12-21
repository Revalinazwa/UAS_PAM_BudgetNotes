package uas.pam.budgetnotes2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val addButton = findViewById<FloatingActionButton>(R.id.addButton)

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        findViewById<RecyclerView>(R.id.recycleview).apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        // Geser untuk menghapus transaksi
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(findViewById(R.id.recycleview))

        addButton.setOnClickListener{
           val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            transactions = db.transactionDao().getAll()
            withContext(Dispatchers.Main) {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun updateDashboard(){
        val totalAmount = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        findViewById<TextView>(R.id.balance).text = getString(R.string.format_currency, totalAmount)
        findViewById<TextView>(R.id.budget).text = getString(R.string.format_currency, budgetAmount)
        findViewById<TextView>(R.id.expense).text = getString(R.string.format_currency, expenseAmount)
    }

    private fun undoDelete(){
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().insertAll(deletedTransaction)
            transactions = oldTransactions

            withContext(Dispatchers.Main){
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun showSnackBar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaksi berhasil dihapus!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Pulihkan"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldTransactions = transactions

        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().delete(transaction)
            transactions = transactions.filter { it.id != transaction.id }
            withContext(Dispatchers.Main){
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackBar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}