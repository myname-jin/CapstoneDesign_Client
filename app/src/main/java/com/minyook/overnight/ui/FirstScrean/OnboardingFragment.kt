package com.minyook.test1.ui.FirstScrean

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.minyook.overnight.R

class OnboardingFragment : Fragment() {

    private var title: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            description = it.getString(ARG_DESCRIPTION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        val tvTitle: TextView = view.findViewById(R.id.tv_onboarding_title)
        val tvDescription: TextView = view.findViewById(R.id.tv_onboarding_description)

        tvTitle.text = title
        tvDescription.text = description

        return view
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"

        @JvmStatic
        fun newInstance(title: String, description: String) =
            OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                }
            }
    }
}