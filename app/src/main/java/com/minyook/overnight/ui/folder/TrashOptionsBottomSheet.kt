package com.minyook.overnight.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.minyook.overnight.R

class TrashOptionsBottomSheet : BottomSheetDialogFragment() {

    interface TrashOptionListener {
        fun onRestore(folderTitle: String)
    }

    private var folderTitle: String = ""

    companion object {
        private const val ARG_TITLE = "folder_title"
        fun newInstance(title: String) = TrashOptionsBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_TITLE, title) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folderTitle = arguments?.getString(ARG_TITLE) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_trash_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tv_trash_options_title).text = folderTitle

        // 복구하기 버튼 클릭
        view.findViewById<LinearLayout>(R.id.option_restore).setOnClickListener {
            (targetFragment as? TrashOptionListener)?.onRestore(folderTitle)
            dismiss()
        }
    }
}