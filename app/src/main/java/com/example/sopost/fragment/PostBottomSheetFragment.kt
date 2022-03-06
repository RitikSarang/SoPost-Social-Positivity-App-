package com.example.sopost.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sopost.R
import com.example.sopost.model.Report
import com.example.sopost.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_post_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_post_bottom_sheet.view.*
import java.text.SimpleDateFormat
import java.util.*


class PostBottomSheetFragment : BottomSheetDialogFragment() {
    var id: String = ""
    private lateinit var postViewModel: PostViewModel
    private val db = FirebaseFirestore.getInstance()
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
            postViewModel.getReportTextStatus(it)
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


}
