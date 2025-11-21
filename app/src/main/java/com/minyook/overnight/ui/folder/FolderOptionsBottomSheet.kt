// ui.folder/FolderOptionsBottomSheet.kt

package com.minyook.overnight.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.minyook.overnight.R

class FolderOptionsBottomSheet : BottomSheetDialogFragment() {

    // 1. 콜백 인터페이스 정의
    interface FolderOptionListener {
        fun onFolderDeleted(folderTitle: String)
        fun onFolderRenamed(folderTitle: String)
    }

    // 2. 폴더 제목을 받기 위한 인스턴스 생성자
    companion object {
        private const val ARG_TITLE = "folder_title"
        fun newInstance(title: String) = FolderOptionsBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_TITLE, title) }
        }
    }

    private val folderTitle: String? by lazy {
        arguments?.getString(ARG_TITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_folder_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. 리스너 연결 (Target Fragment를 찾아 콜백을 전달)
        val listener = targetFragment as? FolderOptionListener ?: activity as? FolderOptionListener

        // 1. 이름 바꾸기 옵션 리스너
        view.findViewById<LinearLayout>(R.id.option_rename).setOnClickListener {
            listener?.onFolderRenamed(folderTitle ?: "")
            dismiss()
        }

        // 2. 휴지통으로 이동 옵션 리스너 (삭제)
        view.findViewById<LinearLayout>(R.id.option_delete).setOnClickListener {
            if (folderTitle != null && listener != null) {
                listener.onFolderDeleted(folderTitle!!) // Fragment에 삭제 요청 전달
            } else {
                Toast.makeText(context, "삭제할 폴더 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }
}