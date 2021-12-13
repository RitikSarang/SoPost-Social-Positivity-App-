package com.example.sopost.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sopost.HomeActivity
import com.example.sopost.R
import kotlinx.android.synthetic.main.fragment_second.view.*

const val COMPLETED = "COMPLETED"

class SecondScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second, container, false)


        view.backButton.setOnClickListener {
            //findNavController().navigate(R.id.action_viewPagerFragment_to_loginFragment)
            onBoardingFinished()
            startActivity(Intent(context, HomeActivity::class.java))
            requireActivity().finishAffinity()
        }

        return view
    }

    private fun onBoardingFinished() {
        val sharedPref = activity?.getSharedPreferences("SoPost", Context.MODE_PRIVATE)
        sharedPref?.let {
            with(sharedPref.edit()) {
                putBoolean(COMPLETED, true)
                    .apply()
            }
        }
    }

}