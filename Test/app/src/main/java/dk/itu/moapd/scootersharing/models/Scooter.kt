package dk.itu.moapd.scootersharing.models

import android.icu.text.SimpleDateFormat
import java.util.*

class Scooter(var name: String? = null, var where: String? = null, var timestamp: Long? = null) {

    override fun toString () : String {
        return " $name is placed at $where the ${timestamp?.toDateString()}"
    }

    fun Long.toDateString() : String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

}