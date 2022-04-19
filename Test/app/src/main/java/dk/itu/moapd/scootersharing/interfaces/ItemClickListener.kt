package dk.itu.moapd.scootersharing.interfaces
import dk.itu.moapd.scootersharing.models.Scooter

interface ItemClickListener  {
    fun onItemClickListener(scooter: Scooter, position: Int)
}