package dk.itu.moapd.scootersharing.adapters

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.models.Scooter
import dk.itu.moapd.scootersharing.utils.BUCKET_URL
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
        }
    }

    fun Long.toDateString() : String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}
/*
val storage = Firebase.storage(BUCKET_URL)
val imageRef = storage.reference.child("${scooter.path}")
imageRef.downloadUrl.addOnSuccessListener { url ->
    Glide.with(holder.imageView.context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop().into(holder.imageView)
}*/