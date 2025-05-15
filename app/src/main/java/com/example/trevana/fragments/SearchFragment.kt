class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private var activeSearchListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupPriceFilter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeSearchListener?.remove()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts) { post ->
            navigateToPostDetail(post.id)
        }
        binding.rvResults.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        // Handle keyboard search action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Handle search button click
        binding.btnSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun setupPriceFilter() {
        // Add input filters for price fields
        arrayOf(binding.etMinPrice, binding.etMaxPrice).forEach { editText ->
            editText.filters = arrayOf<InputFilter>(InputFilterMinMax(0.0, 999999.0))
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        val minPrice = binding.etMinPrice.text.toString().toDoubleOrNull() ?: 0.0
        var maxPrice = binding.etMaxPrice.text.toString().toDoubleOrNull() ?: 999999.0

        // Handle invalid price range
        if (maxPrice < minPrice) maxPrice = minPrice

        activeSearchListener?.remove()
        showLoading(true)

        // Build Firestore query
        val baseQuery = Firebase.firestore.collection("posts")
            .whereGreaterThanOrEqualTo("price", minPrice)
            .whereLessThanOrEqualTo("price", maxPrice)

        val finalQuery = if (query.isNotEmpty()) {
            baseQuery.whereArrayContainsAny("searchKeywords", query.split(" "))
        } else {
            baseQuery
        }

        activeSearchListener = finalQuery.addSnapshotListener { snapshot, error ->
            showLoading(false)

            when {
                error != null -> {
                    ToastHelper.showError(requireContext(), "Search failed: ${error.message}")
                    updateEmptyState(visible = true)
                }
                snapshot != null && !snapshot.isEmpty -> {
                    posts.clear()
                    posts.addAll(snapshot.toObjects(Post::class.java))
                    adapter.notifyDataSetChanged()
                    updateEmptyState(visible = false)
                }
                else -> {
                    updateEmptyState(visible = true)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSearch.isEnabled = !isLoading
        binding.btnSearch.text = if (isLoading) "Searching..." else getString(R.string.search)
    }

    private fun updateEmptyState(visible: Boolean) {
        binding.tvEmptyState.visibility = if (visible) View.VISIBLE else View.GONE
        binding.rvResults.visibility = if (visible) View.GONE else View.VISIBLE
    }

    private fun navigateToPostDetail(postId: String) {
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToPostDetailFragment(postId)
        )
    }

    // Input filter for price validation
    private inner class InputFilterMinMax(private val min: Double, private val max: Double) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toDouble()
                if (isInRange(min, max, input)) return null
            } catch (e: NumberFormatException) {
                // Invalid number
            }
            return ""
        }

        private fun isInRange(min: Double, max: Double, value: Double): Boolean {
            return value in min..max
        }
    }
}