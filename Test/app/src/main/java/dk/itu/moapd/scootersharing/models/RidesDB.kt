package dk.itu.moapd.scootersharing
import android . content . Context
import dk.itu.moapd.scootersharing.models.Scooter
import java . util . Random
import kotlin . collections . ArrayList
class RidesDB private constructor ( context : Context ) {
    private val rides = ArrayList <Scooter>()
    companion object : RidesDBHolder<RidesDB, Context>(:: RidesDB )
    init {
        rides . add (
            Scooter ("Chuck Norris", "ITU", randomDate () )
        )
        rides . add (
            Scooter ("Bruce Lee", "Fields", randomDate () )
        )
        rides . add (
            Scooter ("Rambo", "Kobenhavns Lufthavn", randomDate () )
        )
        // TODO : You can add more ‘Scooter ‘ objects if you want to.
    }
    fun getScooters () : List <Scooter> {
        return rides
    }
    fun addScooter ( name : String , where : String ) {
        rides.add(Scooter(name, where, System.currentTimeMillis()))
    }
    fun updateScooter ( name : String , where : String ) {
        for (Scooter in rides) {
            if (Scooter.name == name) {
                addScooter(name, where)
                rides.remove(Scooter)
                break
            }
        }
    }
    fun getLastScooterInfo () : String {
        return rides.get(rides.size-1).toString()
    }
    /**
     * Get the current timestamp and generate a random data in the
    ,→ last 365 days .
     *
     * @return A random timestamp in the last year .
     */
    private fun randomDate () : Long {
        val random = Random ()
        val now = System . currentTimeMillis ()
        val year = random . nextDouble () * 1000 * 60 * 60 * 24 * 365
        return ( now - year ) . toLong ()
    }
}
open class RidesDBHolder < out T : Any , in A >( creator : ( A ) -> T ) {
    private var creator : (( A ) -> T ) ? = creator
    @Volatile private var instance : T ? = null
    fun get ( arg : A ) : T {
        val checkInstance = instance
        if ( checkInstance != null )
            return checkInstance
        return synchronized ( this ) {
            val checkInstanceAgain = instance
            if ( checkInstanceAgain != null )
                checkInstanceAgain
            else {
                val created = creator !!( arg )
                instance = created
                creator = null
                created
            }
        }
    }
}