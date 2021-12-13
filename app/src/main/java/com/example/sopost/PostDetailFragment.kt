package com.example.sopost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import com.example.sopost.model.Post
import com.example.sopost.utils.Utils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_post_detail.*


class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postID = arguments?.getString("PROFILEPOSTID")
        postID?.let {
            setUpData(postID)
        }
    }

    private fun setUpData(postID: String) {
        getPostById(postID).addOnCompleteListener { post ->
            if(post.isComplete){
                val data = post.result?.toObject(Post::class.java)
                data?.let {
                    val fb = FirebaseStorage.getInstance().reference.child(data.user.imageURL)

                    fb.downloadUrl.addOnSuccessListener { imgURL ->
                        Glide
                            .with(requireContext())
                            .load(imgURL.toString())
                            .centerCrop()
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .into(img_prof)
                    }

                    Glide
                        .with(requireContext())
                        .load(data.imageURL)
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_cloud_upload_24)
                        .into(img_post)


                    txt_name.text = data.user.name
                    txt_bio.text = data.user.email
                    txt_postTile.text = data.title
                    txt_postDesc.text = data.desc
                    txt_likes.text = data.likedBy.size.toString()
                    txt_createdAt.text = Utils.getTimeAgo(data.createAt)
                }
            }
        }

        val  builder = Zoomy.Builder(requireActivity())
            .target(img_post)
            .animateZooming(false)
            .enableImmersiveMode(false)
            .tapListener{
                Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
            }
        builder.register()
    }

    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val postCollection = db.collection("postsbyuser")
        val uid = FirebaseAuth.getInstance().currentUser!!.uid


        return postCollection.document(uid)
            .collection("1")
            .document(postId)
            .get()
    }
}
