// ui.folder/RenameFolderDialogFragment.kt

package com.minyook.overnight.ui.folder

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.minyook.overnight.R // R 파일 경로 확인

class RenameFolderDialogFragment : DialogFragment() {

    // -----------------------------------
    // 1. 콜백 인터페이스 정의 (FolderFragment로 결과 전달)
    // -----------------------------------
    interface RenameListener {
        fun onFolderRenamed(oldTitle: String, newTitle: String)
    }

    // -----------------------------------
    // 2. 인스턴스 생성 및 데이터 전달
    // -----------------------------------
    private var currentFolderName: String? = null

    companion object {
        private const val ARG_CURRENT_NAME = "current_name"

        fun newInstance(currentName: String): RenameFolderDialogFragment {
            return RenameFolderDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CURRENT_NAME, currentName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFolderName = arguments?.getString(ARG_CURRENT_NAME)
    }

    // -----------------------------------
    // 3. 다이얼로그 생성 (onCreateDialog)
    // -----------------------------------
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        // dialog_rename_folder.xml 인플레이트
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_rename_folder, null)

        val editText = view.findViewById<EditText>(R.id.et_new_folder_name)

        // 현재 폴더 이름을 힌트나 기본값으로 설정
        editText.setText(currentFolderName)
        editText.setSelection(editText.text.length) // 커서를 끝으로 이동

        builder.setView(view)
            .setTitle("폴더 이름 변경 (${currentFolderName})") // 다이얼로그 제목 설정
            .setPositiveButton("변경") { _, _ ->
                val newName = editText.text.toString().trim()

                if (newName.isEmpty() || newName == currentFolderName) {
                    Toast.makeText(context, "새로운 이름을 입력하거나 취소하세요.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // 리스너를 통해 FolderFragment에 데이터 전달
                val listener = targetFragment as? RenameListener ?: activity as? RenameListener
                if (listener != null && currentFolderName != null) {
                    listener.onFolderRenamed(currentFolderName!!, newName)
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }
}