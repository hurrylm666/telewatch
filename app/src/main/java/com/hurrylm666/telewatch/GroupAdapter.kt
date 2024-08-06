import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hurrylm666.telewatch.R

class GroupAdapter(private val items: List<String>) : RecyclerView.Adapter<GroupAdapter.SimpleViewHolder>() {

    class SimpleViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_textview, parent, false) as TextView
        return SimpleViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.textView.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}