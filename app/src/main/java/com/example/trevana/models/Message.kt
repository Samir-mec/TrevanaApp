data class Message(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp = Timestamp.now()
)