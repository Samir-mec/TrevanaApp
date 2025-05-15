package com.example.trevana.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.trevana.R
import com.example.trevana.databinding.ItemPostBinding
import com.example.trevana.fragments.HomeFragmentDirections
import com.example.trevana.models.Post

class PostAdapter(private var postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val post = postList[adapterPosition]
                val action = HomeFragmentDirections.actionNavHomeToNavPostDetail(post.id)
                it.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]

        with(holder.binding) {
            // Required Fields
            tvTitle.text = post.title
            tvPrice.text = "$${"%.2f".format(post.price)}"
            tvCategory.text = post.category

            // Status Display
            tvStatus.text = post.status.uppercase()
            when (post.status.lowercase()) {
                "active" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.success_green))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_active)
                }
                "sold" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.error_red))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_sold)
                }
                "expired" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.disabled))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_expired)
                }
            }

            // Location
            tvLocation.text = post.location.ifEmpty { root.context.getString(R.string.no_location) }
            tvLocation.visibility = if (post.location.isNotEmpty()) View.VISIBLE else View.GONE

            // Image Loading
            if (post.images.isNotEmpty()) {
                Glide.with(root.context)
                    .load(post.images.first())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.ic_image_error)
                    .into(ivPostImage)
                ivPostImage.visibility = View.VISIBLE
            } else {
                ivPostImage.setImageResource(R.drawable.ic_no_image)
                ivPostImage.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: List<Post>) {
        postList = newPosts
        notifyDataSetChanged()
    }
}