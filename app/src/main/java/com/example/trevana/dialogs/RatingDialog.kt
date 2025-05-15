class RatingDialog(
    context: Context,
    private val sellerId: String
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_rating)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.et_comment)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            val rating = Rating(
                sellerId = sellerId,
                raterId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                stars = ratingBar.rating,
                comment = etComment.text.toString()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(sellerId)
                .collection("ratings")
                .add(rating)
                .addOnSuccessListener {
                    Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to submit rating", Toast.LENGTH_SHORT).show()
                }
        }
    }
}