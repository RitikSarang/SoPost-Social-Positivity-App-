package com.example.sopost.Adapter

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sopost.R
import com.example.sopost.model.Likes
import com.example.sopost.model.Post
import com.example.sopost.utils.Utils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.list_item_post.view.*
import kotlinx.android.synthetic.main.list_item_post.view.img_post
import kotlinx.android.synthetic.main.list_profile_item.view.*
import kotlinx.android.synthetic.main.top_user_item_layout.view.*
import java.io.File

class TopUserAdapter(options: FirestoreRecyclerOptions<Likes>) :
    FirestoreRecyclerAdapter<Likes, TopUserAdapter.TopUserViewHolder>(
        options
    ) {

    class TopUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, model: Likes) {
            with(model) {
                if(position == 0){
                    shineAnimation()
                    itemView.cardView.setCardBackgroundColor(Color.GREEN)
                    itemView.item_txtName.setTextColor(Color.WHITE)
                    itemView.item_likes.setTextColor(Color.WHITE)
                }
                itemView.item_txtName.text = name
                itemView.item_likes.text = likes

                val fbProfileImage =
                    FirebaseStorage.getInstance().reference.child(img_URL)
                fbProfileImage.downloadUrl.addOnSuccessListener { imgURL ->
                    Glide
                        .with(itemView.context)
                        .load(imgURL.toString())
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(itemView.item_profImg)


                    when (likes.toInt()) {
                        in 1..2 -> {
                            itemView.img_badge.setImageDrawable(
                                ContextCompat.getDrawable(
                                    itemView.img_badge.context,
                                    R.drawable.diamond
                                )
                            )

                        }
                        in 3..4 -> {
                            itemView.img_badge.setImageDrawable(
                                ContextCompat.getDrawable(
                                    itemView.img_badge.context,
                                    R.drawable.legendary
                                )
                            )
                        }
                        in 5..6 -> {
                            itemView.img_badge.setImageDrawable(
                                ContextCompat.getDrawable(
                                    itemView.img_badge.context,
                                    R.drawable.peace
                                )
                            )
                        }
                        in 31..40 -> {

                            itemView.img_badge.setImageDrawable(
                                ContextCompat.getDrawable(
                                    itemView.img_badge.context,
                                    R.drawable.god
                                )
                            )
                        }
                        else -> {

                            itemView.img_badge.setImageDrawable(
                                ContextCompat.getDrawable(
                                    itemView.img_badge.context,
                                    R.drawable.basic
                                )
                            )

                        }
                    }

                }
            }
        }
        private fun shineAnimation() {
            // attach the animation layout Using AnimationUtils.loadAnimation
            val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.left_right)
            itemView.shine.startAnimation(anim)
            // override three function There will error
            // line below the object
            // click on it and override three functions
            anim.setAnimationListener(object : Animation.AnimationListener {
                // This function starts the
                // animation again after it ends
                override fun onAnimationEnd(p0: Animation?) {
                    itemView.shine.startAnimation(anim)
                }

                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationRepeat(p0: Animation?) {}

            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopUserViewHolder {
        val view = TopUserViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.top_user_item_layout, parent, false)
        )
        return view
    }

    override fun onBindViewHolder(holder: TopUserViewHolder, position: Int, model: Likes) {
        holder.bind(position, model)
    }


}