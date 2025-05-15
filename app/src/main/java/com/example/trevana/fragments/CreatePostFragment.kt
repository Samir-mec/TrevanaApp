class CreatePostFragment : Fragment() {
    private val PICK_IMAGES = 101
    private val imageUris = mutableListOf<Uri>()
    private lateinit var adapter: ImageAdapter
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageRecyclerView()
        setupClickListeners()
    }

    private fun setupImageRecyclerView() {
        adapter = ImageAdapter(imageUris)
        binding.rvImages.apply {
            adapter = this@CreatePostFragment.adapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupClickListeners() {
        binding.btnUpload.setOnClickListener { openImagePicker() }
        binding.btnSubmit.setOnClickListener { validateAndCreatePost() }
    }

    private fun openImagePicker() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(this, PICK_IMAGES)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let { handleSelectedImages(it) }
        }
    }

    private fun handleSelectedImages(data: Intent) {
        data.clipData?.let { clip ->
            (0 until clip.itemCount).map { clip.getItemAt(it).uri }.let {
                imageUris.addAll(it)
            }
        } ?: data.data?.let { uri ->
            imageUris.add(uri)
        }
        adapter.notifyDataSetChanged()
    }

    private fun validateAndCreatePost() {
        val title = binding.etTitle.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val category = binding.spCategory.selectedItem?.toString() ?: ""
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (!validateInputs(title, priceText, category, location)) return

        binding.btnSubmit.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val searchKeywords = generateSearchKeywords(title, description)

        if (imageUris.isNotEmpty()) {
            uploadImagesAndCreatePost(title, priceText.toDouble(), category, description, location, searchKeywords)
        } else {
            savePostToFirestore(title, priceText.toDouble(), category, description, location, searchKeywords, emptyList())
        }
    }

    private fun generateSearchKeywords(title: String, description: String): List<String> {
        return (title.split(" ") + description.split(" "))
            .map { it.lowercase().replace(Regex("[^a-z0-9]"), "") }
            .filter { it.length > 2 }
            .distinct()
    }

    private fun validateInputs(title: String, priceText: String, category: String, location: String): Boolean {
        return when {
            title.isEmpty() -> {
                binding.etTitle.error = "Title required"
                false
            }
            priceText.isEmpty() -> {
                binding.etPrice.error = "Price required"
                false
            }
            priceText.toDoubleOrNull() == null -> {
                binding.etPrice.error = "Invalid price format"
                false
            }
            category.isEmpty() -> {
                ToastHelper.showError(requireContext(), "Select a category")
                false
            }
            location.isEmpty() -> {
                binding.etLocation.error = "Location required"
                false
            }
            else -> true
        }
    }

    private fun uploadImagesAndCreatePost(
        title: String,
        price: Double,
        category: String,
        description: String,
        location: String,
        searchKeywords: List<String>
    ) {
        val storage = Firebase.storage
        val imageUrls = mutableListOf<String>()
        var uploadCount = 0

        imageUris.forEach { uri ->
            val ref = storage.reference.child("posts/${UUID.randomUUID()}")
            ref.putFile(uri)
                .continueWithTask { task ->
                    task.result?.storage?.downloadUrl
                }
                .addOnSuccessListener { url ->
                    imageUrls.add(url.toString())
                    if (++uploadCount == imageUris.size) {
                        savePostToFirestore(title, price, category, description, location, searchKeywords, imageUrls)
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    ToastHelper.showError(requireContext(), "Image upload failed")
                }
        }
    }

    private fun savePostToFirestore(
        title: String,
        price: Double,
        category: String,
        description: String,
        location: String,
        searchKeywords: List<String>,
        images: List<String>
    ) {
        val post = Post(
            title = title,
            price = price,
            category = category,
            description = description,
            images = images,
            userId = Firebase.auth.currentUser?.uid ?: "",
            timestamp = Timestamp.now(),
            location = location,
            searchKeywords = searchKeywords,
            status = "active"
        )

        Firebase.firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                ToastHelper.showSuccess(requireContext(), "Post created successfully")
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                ToastHelper.showError(requireContext(), "Error: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}