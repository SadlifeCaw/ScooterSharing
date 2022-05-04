package dk.itu.moapd.scootersharing.utils

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

const val BUCKET_URL = "gs://scooter-sharing-2ac71.appspot.com"
const val DATABASE_URL = "https://scooter-sharing-2ac71-default-rtdb.europe-west1.firebasedatabase.app/"

class MyAplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
    }
}