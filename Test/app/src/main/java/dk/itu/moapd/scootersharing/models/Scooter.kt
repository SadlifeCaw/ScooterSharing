package dk.itu.moapd.scootersharing.models

import android.icu.text.SimpleDateFormat
import java.util.*

class Scooter(var id: String? = null,
              var lat: Double? = null,
              var lon: Double? = null,
              var battery: Int? = null,
              var timestamp: Long? = null,
              var model: String? = null) {
    var where = ""
    var available = true

    override fun toString () : String {
        return " $id is placed at $where the ${timestamp?.toDateString()}"
    }

    fun Long.toDateString() : String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}