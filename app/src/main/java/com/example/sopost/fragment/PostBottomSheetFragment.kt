package com.example.sopost.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sopost.R
import com.example.sopost.model.Post
import com.example.sopost.model.Report
import com.example.sopost.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_post_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_post_bottom_sheet.view.*
import java.text.SimpleDateFormat
import java.util.*


class PostBottomSheetFragment : BottomSheetDialogFragment() {
    var id: String = ""
    private lateinit var postViewModel: PostViewModel
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val postCollection = db.collection("posts")
    val postCollectionForUser = db.collection("postsbyuser")
    val storageReference = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(PostViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_post_bottom_sheet, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //take uid frmo adapter and give it to report
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val now = Date()
        val formatDateForPosts =
            SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(now)
        db.collection("Mod").document(dateForPosts)
            .collection(uid)
            .get().addOnCompleteListener {
                if (it.isComplete) {
                    txt_delete.isVisible = !it.result!!.isEmpty
                }
            }



        val postId = arguments?.getString(POSTID)
        postId?.let {
            postViewModel.getReportTextStatus(it)
            txt_delete.setOnClickListener {
                //delete post
                getPostById(postId).addOnCompleteListener {
                    val post = it.result?.toObject(Post::class.java)
                    post?.let {
                        deletePost(postId, post.uid, post.imageStorageRef)
                    }
                }
            }
        }
        postViewModel.reportStatus.observe(viewLifecycleOwner){ report ->
            report?.let {  nullSafeReport ->
                val currentUserId = auth.currentUser!!.uid
                val tempUID = nullSafeReport.report.contains(currentUserId)
                top_l.isVisible = nullSafeReport.uid != currentUserId
                if(top_l.isVisible) {
                    if (tempUID) {
                        view.txt_report.text = "UnReport"
                    } else {
                        view.txt_report.text = "Report"
                    }
                }
            }
        }
        top_l.setOnClickListener {
            //Toast.makeText(context, "${postId}", Toast.LENGTH_SHORT).show()
            //check here if reported or unreported
            postId?.let {
                postViewModel.setReport(postId)
            }
            findNavController().popBackStack()
        }
    }
    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        return postCollection.document(dateForPosts)
            .collection("1")
            .document(postId)
            .get()
    }

    private fun deletePost(postId: String, uid: String, filePath: String) {

        //Log.i("postdeleted", "deletePost: $postId $uid")
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())


        postCollection.document(dateForPosts).collection("1")
            .document(postId).delete().addOnCompleteListener {
                if (it.isComplete) {
                    postCollectionForUser.document(uid).collection("1")
                        .document(postId).delete().addOnCompleteListener {
                            storageReference.getReference(filePath).delete().addOnCompleteListener {
                                Toast.makeText(
                                    context,
                                    "Posted has been removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }


    }
}
