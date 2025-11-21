// ui.folder/ChildNotesFragment.kt
package com.minyook.overnight.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.minyook.overnight.R

class ChildNotesFragment : Fragment() {

    private var folderTitle: String? = null

    companion object {
        private const val ARG_TITLE = "folder_title"
        fun newInstance(title: String) = ChildNotesFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            folderTitle = it.getString(ARG_TITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // item_note_child.xml 레이아웃 인플레이트 (이전에 제공해주신 XML)
        return inflater.inflate(R.layout.item_note_child, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 제목 설정
        view.findViewById<TextView>(R.id.tv_child_title).text = folderTitle

        // 2. 툴바 리스너 및 FAB 설정
        setupToolbarAndFab(view)

        // TODO: 3. RecyclerView Adapter 연결 및 데이터 로드 (녹음된 노트 파일)
    }

    private fun setupToolbarAndFab(view: View) {
        // 뒤로가기 버튼: 이전 프래그먼트로 돌아감
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // '더 보기' 버튼: 폴더 옵션 바텀 시트 띄우기
        view.findViewById<ImageButton>(R.id.btn_more).setOnClickListener {
            val bottomSheet = FolderOptionsBottomSheet.newInstance(folderTitle ?: "")
            bottomSheet.setTargetFragment(this, 0)
            bottomSheet.show(parentFragmentManager, "FolderOptions")
        }

        // ⭐ [수정] FAB (새 노트 추가) 리스너 설정 및 숨기기
        val fabAddNote = view.findViewById<ImageButton>(R.id.fab_add_note)

        // 1. FAB 숨기기
        fabAddNote.visibility = View.GONE

        // 2. 리스너는 이제 불필요하지만, 혹시 모를 대비
        fabAddNote.setOnClickListener {
            // TODO: 파일 업로드/새 녹음 시작 로직 구현
        }
    }
}