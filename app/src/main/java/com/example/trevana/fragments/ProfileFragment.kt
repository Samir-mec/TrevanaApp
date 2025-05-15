package com.example.trevana.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.trevana.R
import com.example.trevana.adapters.PostAdapter
import com.example.trevana.databinding.FragmentProfileBinding
import com.example.trevana.dialogs.RatingDialog
import com.example.trevana.models.Post
import com.example.trevana.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadUserData()
        setupRatingButton()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(emptyList())
        binding.rvPosts.adapter = adapter
        binding.rvPosts.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun loadUserData() {
        currentUser?.uid?.let { userId ->
            // Load user profile data
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    user?.let {
                        binding.tvUsername.text = it.username
                        binding.tvEmail.text = it.email
                        binding.tvPhone.text = it.phone ?: "Not provided"

                        // Load profile picture
                        if (!it.profilePicture.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(it.profilePicture)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(binding.ivProfile)
                        }

                        // Format join date
                        val joinDate = it.joinDate?.toDate()?.let { date ->
                            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
                        } ?: "Unknown"
                        binding.tvJoinDate.text = getString(R.string.joined_date, joinDate)

                        // Load ratings
                        db.collection("users").document(userId).collection("ratings")
                            .get()
                            .addOnSuccessListener { ratings ->
                                val avgRating = ratings.documents
                                    .mapNotNull { it.getDouble("stars")?.toFloat() }
                                    .average()
                                    .takeIf { !it.isNaN() } ?: 0.0
                                binding.ratingBar.rating = avgRating.toFloat()
                                binding.tvRating.text = "%.1f".format(avgRating)
                            }
                    }
                }

            // Load user's posts
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val posts = documents.map { doc ->
                        doc.toObject(Post::class.java).copy(id = doc.id)
                    }
                    adapter.updatePosts(posts)
                }
        } ?: run {
            // Handle not logged in
        }
    }

    private fun setupRatingButton() {
        // Example: Add rating button if viewing another user's profile
        // For current user profile, hide the button
        binding.btnRateUser.visibility = View.GONE  // Default hidden

        // Uncomment if implementing rating for other users:
        // binding.btnRateUser.setOnClickListener {
        //     RatingDialog(requireContext(), userIdToRate).show()
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Extension function for PostAdapter
    private fun PostAdapter.updatePosts(posts: List<Post>) {
        this.postList = posts
        notifyDataSetChanged()
    }
}