package dk.itu.moapd.scootersharing.adapters

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.models.Scooter
import java.util.*

class RidesListXX(options: FirebaseRecyclerOptions<Scooter>) :
    FirebaseRecyclerAdapter<Scooter, RidesListXX.ViewHolder>(options) {

    var onItemClick: ((Scooter) -> Unit)? = null
    var scooters: MutableList<Scooter> = mutableListOf()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.name1)
        val where: TextView = view.findViewById(R.id.where1)
        val timestamp: TextView = view.findViewById(R.id.timestamp1)
        val imageView: ImageView = view.findViewById(R.id.image_view)

        init {
            view.setOnClickListener{
                onItemClick?.invoke(scooters[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_rides, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, scooter: Scooter) {
        scooters.add(scooter)
        holder.apply {
            id.text = scooter.id
            where.text = scooter.where
            timestamp.text = scooter.timestamp?.toDateString()

            // Extremely bad implementation of showing specific pictures. Bad for expansion, but functionality is the game in our case. (Done because of a lack of time.)
            if (scooter.imgstring == "kaabo_mantis_10_pro"){
                imageView.setImageResource(R.drawable.kaabo_mantis_10_pro)
            } else if (scooter.imgstring == "vsett_9_13ah"){
                imageView.setImageResource(R.drawable.vsett_9_13ah)
            } else if (scooter.imgstring == "xiaomi_pro_2"){
                imageView.setImageResource(R.drawable.xiaomi_pro_2)
            }
        }
    }

    fun Long.toDateString() : String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}