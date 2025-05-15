package com.example.trevana.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.trevana.R
import com.example.trevana.adapters.ImageSliderAdapter
import com.example.trevana.databinding.FragmentPostDetailBinding
import com.example.trevana.models.Post
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PostDetailFragmentArgs by navArgs()
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPostDetails()
        setupClickListeners()
    }

    private fun loadPostDetails() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("posts").document(args.postId)
            .get()
            .addOnSuccessListener { document ->
                val post = document.toObject(Post::class.java)
                post?.let { updateUI(it) }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                // Handle error
            }
    }

    private fun updateUI(post: Post) {
        binding.apply {
            progressBar.visibility = View.GONE

            // Image Slider
            val adapter = ImageSliderAdapter(post.images)
            viewPagerImages.adapter = adapter
            viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    tvImageCounter.text = "${position + 1}/${post.images.size}"
                }
            })

            // Post Details
            tvTitle.text = post.title
            tvPrice.text = "$${"%.2f".format(post.price)}"
            tvCategory.text = post.category
            tvDescription.text = post.description
            tvLocation.text = post.location

            // Seller Info
            loadSellerInfo(post.userId)
        }
    }

    private fun loadSellerInfo(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let {
                    binding.tvSellerName.text = it.username
                    binding.tvMemberSince.text = "Member since ${it.joinDate?.toDate()?.formatDate()}"
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnContactSeller.setOnClickListener {
            // Implement chat system later
        }

        binding.btnRateSeller.setOnClickListener {
            val dialog = RatingDialog(requireContext(), args.postId)
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Extension function for date formatting
fun Date.formatDate(): String {
    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(this)
}