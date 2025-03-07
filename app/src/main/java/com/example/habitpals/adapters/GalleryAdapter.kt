package com.example.habitpals.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.habitpals.R


class GalleryAdapter(private val images: MutableList<String>) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.gallery_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = images.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]
        Log.d("GalleryAdapter", "Binding image URL: $imageUrl")
        holder.imageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image) // Add a placeholder drawable
            error(R.drawable.placeholder_image) // Add an error drawable
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateImages(newImages: List<String>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }
}

