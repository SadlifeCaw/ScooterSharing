package dk.itu.moapd.scootersharing.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.models.Scooter

class RidesListX(private val data: ArrayList<Scooter>) :
    RecyclerView.Adapter<RidesListX.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name1)
        val where: TextView = view.findViewById(R.id.where1)
        val timestamp: TextView = view.findViewById(R.id.timestamp1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_rides, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scooter = data[position]
        holder.apply {
            name.text = scooter.name
            where.text = scooter.where
            timestamp.text = scooter.timestamp.toString()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}