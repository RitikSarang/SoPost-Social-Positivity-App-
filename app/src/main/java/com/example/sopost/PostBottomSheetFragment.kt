package com.example.sopost

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.sopost.Adapter.IPostAdapter
import com.example.sopost.fragment.POSTID
import com.example.sopost.model.Report
import com.example.sopost.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_post_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_post_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_profile_bottom_sheet.*
import kotlinx.coroutines.currentCoroutineContext
import java.text.SimpleDateFormat
import java.util.*


class PostBottomSheetFragment : BottomSheetDialogFragment() {
    var id: String = ""
    private lateinit var postViewModel: PostViewModel
    private val db = FirebaseFirestore.getInstance()
    private val reportCollection = db.collection("Report")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
        val postId = arguments?.getString(POSTID)
        postId?.let {
            getPostReportById(postId).addOnCompleteListener {
                if (it.isComplete) {
                    val report = it.result?.toObject(Report::class.java)
                    report?.let {
                        val currentUserId = auth.currentUser!!.uid
                        val tempUID = it.report.contains(currentUserId)

                        top_l.isVisible = it.uid != currentUserId
                        if(top_l.isVisible) {
                            if (tempUID) {
                                view.txt_report.text = "UnReport"
                            } else {
                                view.txt_report.text = "Report"
                            }
                        }
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

        }
    }

    private fun getPostReportById(postId: String): Task<DocumentSnapshot> {
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        return reportCollection.document(postId).get()
    }
}
