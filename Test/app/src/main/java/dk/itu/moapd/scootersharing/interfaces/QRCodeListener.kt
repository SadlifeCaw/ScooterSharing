package dk.itu.moapd.scootersharing.interfaces

interface QRCodeListener {
    fun QRFound(qrCode: String)
    fun QRNotFound()
}