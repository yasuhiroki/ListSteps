package yasuhiroki.liststeps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StepsAdapter(private val list: MutableList<String>) : RecyclerView.Adapter<StepViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return StepViewHolder(root)
    }

    override fun getItemCount(): Int {
        return list.size

    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.text.text = list[position]
    }

    fun addItem(str: String): Unit {
        list.add(str)
        notifyDataSetChanged()
    }
}

class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var text = itemView.findViewById<TextView>(R.id.text)
}
