package dk.itu.moapd.scootersharing.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.models.Scooter

class RidesListXX(options: FirebaseRecyclerOptions<Scooter>) :
    FirebaseRecyclerAdapter<Scooter, RidesListXX.ViewHolder>(options) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.name1)
        val where: TextView = view.findViewById(R.id.where1)
        val timestamp: TextView = view.findViewById(R.id.timestamp1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_rides, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, scooter: Scooter) {
        holder.apply {
            id.text = scooter.id
            where.text = scooter.where
            timestamp.text = scooter.timestamp.toString()
        }
    }
}