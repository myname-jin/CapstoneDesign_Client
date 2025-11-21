package com.minyook.overnight.ui.folder

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.minyook.overnight.R

class AddChildDialogFragment : DialogFragment() {

    // 1. 콜백 인터페이스
    interface ChildCreationListener {
        fun onChildCreated(groupName: String, childName: String)
    }

    private var parentGroupName: String? = null

    companion object {
        private const val ARG_GROUP_NAME = "group_name"

        fun newInstance(groupName: String): AddChildDialogFragment {
            return AddChildDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_GROUP_NAME, groupName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentGroupName = arguments?.getString(ARG_GROUP_NAME)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater

        // ⭐ [수정] 만드신 dialog_rename_folder.xml 파일을 여기서 불러옵니다.
        val view = inflater.inflate(R.layout.dialog_rename_folder, null)

        // XML 내부의 EditText 찾기 (ID: et_new_folder_name)
        val editText = view.findViewById<EditText>(R.id.et_new_folder_name)

        builder.setView(view)
            // 3. 다이얼로그 버튼 설정
            .setPositiveButton("추가") { _, _ ->
                val newName = editText.text.toString().trim()

                if (newName.isNotEmpty()) {
                    val listener = targetFragment as? ChildCreationListener
                    if (listener != null && parentGroupName != null) {
                        listener.onChildCreated(parentGroupName!!, newName)
                    }
                } else {
                    Toast.makeText(context, "폴더 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }
}