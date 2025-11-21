// ui.folder/TrashNotesFragment.kt (최종)

package com.minyook.overnight.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.minyook.overnight.R
import java.io.Serializable
import java.util.ArrayList

class TrashNotesFragment : Fragment(), TrashOptionsBottomSheet.TrashOptionListener {

    // ⭐ [추가/수정] 뷰 초기화
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var trashAdapter: TrashAdapter
    private var allFolderData: List<FolderItem.Group>? = null

    companion object {
        private const val ARG_DATA = "folder_data"

        fun newInstance(data: List<FolderItem.Group>): TrashNotesFragment {
            return TrashNotesFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATA, ArrayList(data))
                }
            }
        }

        // 데이터 없이 호출되는 경우를 대비한 기본 newInstance (사용 안 함)
        fun newInstance() = TrashNotesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(ARG_DATA)?.let {
            @Suppress("UNCHECKED_CAST")
            allFolderData = it as List<FolderItem.Group>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trash_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뷰 초기화 (fragment_trash_notes.xml의 ID 사용)
        recyclerView = view.findViewById(R.id.recycler_trash_list)
        emptyTextView = view.findViewById(R.id.tv_trash_empty)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar_trash)

        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 2. 레이아웃 매니저 설정
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 3. 데이터 표시 로직 실행
        displayTrashItems()
    }

    private fun displayTrashItems() {
        val allChildren = allFolderData?.flatMap { it.children } ?: emptyList()
        // MutableList로 변환하여 어댑터에 전달
        val trashedItems = allChildren.filter { it.isDeleted }.toMutableList()

        if (trashedItems.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // ⭐ 어댑터 생성 및 클릭 리스너 연결
            trashAdapter = TrashAdapter(trashedItems) { folderTitle ->
                // 옵션 버튼 클릭 시 BottomSheet 띄우기
                val bottomSheet = TrashOptionsBottomSheet.newInstance(folderTitle)
                bottomSheet.setTargetFragment(this, 0)
                bottomSheet.show(parentFragmentManager, "TrashOptions")
            }
            recyclerView.adapter = trashAdapter
        }
    }

    override fun onRestore(folderTitle: String) {
        // 1. 전체 데이터에서 해당 항목을 찾아 isDeleted = false 로 변경
        allFolderData?.forEach { group ->
            group.children.find { it.name == folderTitle }?.isDeleted = false
        }

        // 2. 휴지통 목록(화면)에서 즉시 제거
        trashAdapter.removeItem(folderTitle)

        // 3. 토스트 메시지
        Toast.makeText(context, "'$folderTitle' 복구됨", Toast.LENGTH_SHORT).show()
    }
}