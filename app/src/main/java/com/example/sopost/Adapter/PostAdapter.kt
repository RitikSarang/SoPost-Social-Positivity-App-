package com.example.sopost.Adapter

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import com.example.sopost.MainActivity
import com.example.sopost.R
import com.example.sopost.model.Post
import com.example.sopost.utils.Utils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_post_detail.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.list_item_post.view.*
import kotlinx.android.synthetic.main.list_item_post.view.img_post
import kotlinx.android.synthetic.main.list_profile_item.view.*
import java.io.File

class PostAdapter(options: FirestoreRecyclerOptions<Post>, val listener: IPostAdapter,val requireActivity: FragmentActivity) : FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(
    options
) {

    inner class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int,post: Post) {

            with(post) {
                val auth: FirebaseAuth = FirebaseAuth.getInstance()
                val currentUserUid = auth.currentUser!!.uid
                if(report.size>0 && report.contains(currentUserUid)){
                    itemView.txt_name.apply {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = user.name
                    }
                    itemView.txt_bio.apply {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = user.email
                    }
                    itemView.txt_postTile.apply {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = title
                    }
                    itemView.txt_postDesc.apply {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = desc
                    }
                    itemView.txt_likes.apply {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = likedBy.size.toString()
                    }
                    itemView.img_post.setImageDrawable(ContextCompat.getDrawable(itemView.img_post.context,R.drawable.eye))
                    itemView.img_prof.setImageDrawable(ContextCompat.getDrawable(itemView.img_prof.context,R.drawable.ic_baseline_person_24))

                    itemView.txt_createdAt.text = "Reported"
                    itemView.txt_createdAt.setTextColor(Color.RED)

                    val auth = uid
                    val isLiked = likedBy.contains(auth)
                    if (isLiked) {
                        itemView.img_like.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.img_like.context,
                                R.drawable.ic_baseline_favorite_liked_24
                            )
                        )
                    } else {
                        itemView.img_like.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.img_like.context,
                                R.drawable.ic_outline_favorite_border_24
                            )
                        )
                    }


                    //Log.i("posted", "bind: ${user.bio} ${user.email}")
                }else{

                    itemView.txt_name.text = user.name
                    itemView.txt_bio.text = user.email
                    itemView.txt_postTile.text = title
                    itemView.txt_postDesc.text = desc

                    val fbPostImage =
                        FirebaseStorage.getInstance().reference.child(imageURL)
                    val fbProfileImage =
                        FirebaseStorage.getInstance().reference.child(user.imageURL)

                    val localFile = File.createTempFile("postTempImage", "jpg")

                    Glide
                        .with(itemView.context)
                        .load(imageURL)
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_cloud_upload_24)
                        .into(itemView.img_post)


                    fbProfileImage.downloadUrl.addOnSuccessListener { imgURL ->
                        Glide
                            .with(itemView.context)
                            .load(imgURL.toString())
                            .centerCrop()
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .into(itemView.img_prof)

                    }


                    val  builder = Zoomy.Builder(requireActivity)
                        .target(itemView.img_post)
                        .animateZooming(false)
                        .enableImmersiveMode(false)

                    builder.register()

                    itemView.txt_likes.text = likedBy.size.toString()
                    itemView.txt_createdAt.text = Utils.getTimeAgo(createAt)


                    val auth = uid
                    val isLiked = likedBy.contains(auth)
                    if (isLiked) {
                        itemView.img_like.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.img_like.context,
                                R.drawable.ic_baseline_favorite_liked_24
                            )
                        )
                    } else {
                        itemView.img_like.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.img_like.context,
                                R.drawable.ic_outline_favorite_border_24
                            )
                        )
                    }


                    Log.i("posted", "bind: ${user.bio} ${user.email}")

                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_post, parent, false))

        view.itemView.img_like.setOnClickListener {
            listener.onLikeClicked(snapshots.getSnapshot(view.adapterPosition).id)
        }

        return view
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.bind(position,model)

        holder.itemView.c1.setOnClickListener {
            listener.onProfileClicked(model.uid)
        }

        holder.itemView.img_settings.setOnClickListener {
            listener.onPostClicked(model.postId)
        }

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val isLiked = model.likedBy.contains(uid)
        if(isLiked) {
            holder.itemView.img_like.setImageDrawable(ContextCompat.getDrawable(holder.itemView.img_like.context, R.drawable.ic_baseline_favorite_liked_24))
        } else {
            holder.itemView.img_like.setImageDrawable(ContextCompat.getDrawable(holder.itemView.img_like.context, R.drawable.ic_outline_favorite_border_24))
        }
    }


}

interface IPostAdapter {
    fun onLikeClicked(postId: String)
    fun onProfileClicked(uid:String)
    fun onPostClicked(uid:String)
}