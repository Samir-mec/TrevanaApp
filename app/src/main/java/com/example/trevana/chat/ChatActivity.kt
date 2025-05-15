class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private var chatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receiverId = intent.getStringExtra("receiverId")!!
        val currentUser = Firebase.auth.currentUser?.uid!!
        chatId = listOf(currentUser, receiverId).sorted().joinToString("_")

        setupRecyclerView()
        loadMessages()

        binding.btnSend.setOnClickListener {
            sendMessage(binding.etMessage.text.toString())
            binding.etMessage.setText("")
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.rvMessages.adapter = adapter
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMessages() {
        Firebase.firestore.collection("chats/$chatId/messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documents?.forEach { doc ->
                    val message = doc.toObject(Message::class.java)
                    if (message != null && !messages.contains(message)) {
                        messages.add(message)
                        adapter.notifyItemInserted(messages.size - 1)
                        binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
    }

    private fun sendMessage(text: String) {
        val message = Message(
            text = text,
            senderId = Firebase.auth.currentUser?.uid!!,
            timestamp = Timestamp.now()
        )

        Firebase.firestore.collection("chats/$chatId/messages")
            .add(message)
    }
}