// ui.folder/AddChildDialogFragment.kt
package com.minyook.overnight.ui.folder

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.minyook.overnight.R // R 파일 경로 확인

class AddChildDialogFragment : DialogFragment() {

    // -----------------------------------
    // 1. 콜백 인터페이스 정의 (FolderFragment로 결과 전달)
    // -----------------------------------
    interface ChildCreationListener {
        fun onChildCreated(groupName: String, childName: String)
    }

    // -----------------------------------
    // 2. 인스턴스 생성 및 데이터 전달
    // -----------------------------------
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

    // -----------------------------------
    // 3. 다이얼로그 생성 (onCreateDialog)
    // -----------------------------------
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        // dialog_add_child.xml 인플레이트
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_child, null)

        // 뷰 ID 참조 (XML과 일치시킴)
        val editText = view.findViewById<EditText>(R.id.et_new_child_name)
        val buttonSave = view.findViewById<Button>(R.id.btn_create) // ✅ XML ID: btn_create
        val buttonCancel = view.findViewById<Button>(R.id.btn_cancel) // ✅ XML ID: btn_cancel

        builder.setView(view)
            .setTitle("새로운 폴더 이름 입력")

        val dialog = builder.create()

        // '추가' (저장) 버튼 클릭 리스너
        buttonSave.setOnClickListener {
            val newName = editText.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 리스너를 통해 FolderFragment에 데이터 전달
            val listener = targetFragment as? ChildCreationListener ?: activity as? ChildCreationListener
            if (listener != null && parentGroupName != null) {
                listener.onChildCreated(parentGroupName!!, newName)
                dismiss()
            } else {
                Toast.makeText(context, "호출 프래그먼트가 리스너를 구현하지 않았습니다.", Toast.LENGTH_LONG).show()
            }
        }

        // '취소' 버튼 클릭 리스너
        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }
}