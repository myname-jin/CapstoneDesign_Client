// ui.folder/FolderOptionsBottomSheet.kt
package com.minyook.overnight.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.minyook.overnight.R

class FolderOptionsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_folder_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 이름 바꾸기 옵션 리스너
        view.findViewById<LinearLayout>(R.id.option_rename).setOnClickListener {
            // TODO: 폴더 이름 바꾸기 다이얼로그 호출 로직 구현
            dismiss()
        }

        // 2. 휴지통으로 이동 옵션 리스너
        view.findViewById<LinearLayout>(R.id.option_delete).setOnClickListener {
            // TODO: 휴지통 이동 처리 로직 구현
            dismiss()
        }
    }
}