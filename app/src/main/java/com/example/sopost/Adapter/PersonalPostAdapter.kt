package com.example.sopost.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sopost.R
import com.example.sopost.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.list_item_post.view.*
import kotlinx.android.synthetic.main.list_profile_item.view.*

class PersonalPostAdapter(options: FirestoreRecyclerOptions<Post>, val listener: IPersonalPostAdapter) : FirestoreRecyclerAdapter<Post, PersonalPostAdapter.PersonalPostViewHolder>(
    options
) {
    inner class PersonalPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(model: Post) {
            with(model) {
                Glide
                    .with(itemView.context)
                    .load(imageURL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_cloud_upload_24)
                    .into(itemView.img_personalpost)
            }
            itemView.img_personalpost.setOnClickListener {
                    listener.onPostClicked(model.uid,model.postId)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalPostViewHolder {
        return PersonalPostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_profile_item, parent, false)

        )
    }

    override fun onBindViewHolder(holder: PersonalPostViewHolder, position: Int, model: Post) {
        holder.bind(model)
    }

}

interface IPersonalPostAdapter {
    fun onPostClicked(uid:String,postID:String)
}

