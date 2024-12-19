package uas.pam.budgetnotes2

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        val addTransaction = findViewById<Button>(R.id.addTransaction)
        val labelInput = findViewById<TextInputEditText>(R.id.labelInput)
        val amountInput = findViewById<TextInputEditText>(R.id.amountInput)
        val descriptionInput = findViewById<TextInputEditText>(R.id.descriptionInput)
        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        labelInput.addTextChangedListener {
            if (it!!.count() > 0)
                labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            if (it!!.count() > 0)
                amountLayout.error = null
        }

        addTransaction.setOnClickListener{
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val description = descriptionInput.text.toString()

            if (label.isEmpty())
                labelLayout.error = "Mohon isi kategori"
            else if (amount == null)
                amountLayout.error = "Mohon isi harga"
            else{
                val transaction = Transaction(0, label, amount, description)
                insert(transaction)
            }
        }

        closeButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun  insert(transaction: Transaction){
        val  db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}