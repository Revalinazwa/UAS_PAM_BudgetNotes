package uas.pam.budgetnotes2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private var transaction : List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {
    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view){
        val label : TextView = view.findViewById(R.id.label)
        val amount : TextView = view.findViewById(R.id.amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_layout, parent, false)
        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transaction[position]
        val context = holder.amount.context

        if(transaction.amount >= 0){
            holder.amount.text = "+ Rp%.2f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
        }else {
            holder.amount.text = "- Rp%.2f".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
        holder.label.text = transaction.label
    }

    override fun getItemCount(): Int {
        return transaction.size
    }

    fun setData(transaction: List<Transaction>){
        this.transaction = transaction
        notifyDataSetChanged()
    }
}