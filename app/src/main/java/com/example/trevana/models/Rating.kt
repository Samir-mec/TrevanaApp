data class Rating(
    val sellerId: String = "",
    val raterId: String = "",
    val stars: Float = 0f,
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now()
)