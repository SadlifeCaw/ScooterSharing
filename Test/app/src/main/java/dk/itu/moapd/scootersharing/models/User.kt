package dk.itu.moapd.scootersharing.models

class User(var email: String? = null,
           var displayname: String? = null,
           var rentedScooterID: String? = null,
           var debt: Double? = null) {
}