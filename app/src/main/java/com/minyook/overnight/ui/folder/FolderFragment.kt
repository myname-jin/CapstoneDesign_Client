package com.minyook.overnight.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.minyook.overnight.R

/**
 * 폴더 목록을 관리하는 메인 프래그먼트.
 * AddChildDialogFragment.ChildCreationListener 인터페이스를 구현하여
 * 다이얼로그로부터 새 폴더 생성 이벤트를 받습니다.
 */
class FolderFragment : Fragment(), AddChildDialogFragment.ChildCreationListener {

    private lateinit var folderAdapter: FolderExpandableAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddFolder: FloatingActionButton

    // -----------------------------------
    // 데이터 초기 설정 (FolderData.kt 기반)
    // -----------------------------------
    private fun getInitialData(): MutableList<FolderItem.Group> {
        // (사용자님이 제공해주신 스크린샷 기반 데이터)
        val allNotesChildren = mutableListOf(
            FolderItem.Child(parentId = "G1", name = "글로벌"),
            FolderItem.Child(parentId = "G1", name = "기본 폴더"),
            FolderItem.Child(parentId = "G1", name = "생활속의통계이해"),
            FolderItem.Child(parentId = "G1", name = "소설공"),
            FolderItem.Child(parentId = "G1", name = "운체")
        )
        val allNotesGroup = FolderItem.Group(
            id = "G1",
            name = "전체 노트",
            isExpanded = true, // 초기에는 펼친 상태로 시작
            children = allNotesChildren
        )

        val trash = FolderItem.Group(id = "G4", name = "휴지통", children = mutableListOf())

        return mutableListOf(allNotesGroup, trash)
    }

    // -----------------------------------
    // Fragment 라이프사이클
    // -----------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 1. fragment_folder.xml (사용자님이 제공한 4번째 XML) 인플레이트
        return inflater.inflate(R.layout.fragment_folder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. 뷰 바인딩 (fragment_folder.xml의 ID 사용)
        recyclerView = view.findViewById(R.id.recycler_folder_list)
        fabAddFolder = view.findViewById(R.id.fab_add_folder)

        // 3. 어댑터 초기화 및 콜백 정의
        folderAdapter = FolderExpandableAdapter(
            data = getInitialData(),
            // 3-1. '+' 버튼 클릭 시 (Adapter -> Fragment)
            onAddClicked = { groupName ->
                showAddChildDialog(groupName)
            },
            // 3-2. 자식 항목 클릭 시 (Adapter -> Fragment)
            onChildClicked = { childName ->
                navigateToChildNotes(childName)
            }
        )

        // 4. 리사이클러뷰 설정
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = folderAdapter

        // 5. 하단 FAB 리스너 (파일 업로드 메뉴)
        fabAddFolder.setOnClickListener {
            // TODO: (이전 스크린샷 기준) bottom_sheet_upload_options.xml 팝업 로직 구현
            Toast.makeText(context, "파일 업로드 메뉴 팝업", Toast.LENGTH_SHORT).show()
        }
    }

    // -----------------------------------
    // 다이얼로그 호출 및 화면 이동
    // -----------------------------------

    /**
     * '전체 노트' 그룹의 '+' 버튼을 눌렀을 때 호출됩니다.
     */
    private fun showAddChildDialog(groupName: String) {
        val dialog = AddChildDialogFragment.newInstance(groupName)

        // (중요) 다이얼로그의 리스너를 현재 프래그먼트(this)로 설정합니다.
        // AddChildDialogFragment는 targetFragment를 확인합니다.
        dialog.setTargetFragment(this, 0)

        dialog.show(parentFragmentManager, "AddChildDialog")
    }

    /**
     * 자식 폴더 항목을 클릭했을 때 호출됩니다.
     */
    private fun navigateToChildNotes(folderTitle: String) {
        val fragment = ChildNotesFragment.newInstance(folderTitle)

        // R.id.fragment_container는 OvernightActivity의 메인 FragmentContainerView ID여야 합니다.
        // TODO: OvernightActivity.xml의 <FragmentContainerView> ID로 변경해주세요.
        val containerId = (view?.parent as? ViewGroup)?.id ?: R.id.fragment_container

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null) // 뒤로가기 버튼으로 돌아올 수 있게 함
            .commit()
    }

    // -----------------------------------
    // AddChildDialogFragment.ChildCreationListener 구현
    // -----------------------------------

    /**
     * 다이얼로그에서 '추가' 버튼을 누르면 이 콜백이 실행됩니다.
     */
    override fun onChildCreated(groupName: String, childName: String) {
        // 어댑터에 새 항목을 추가하도록 알림
        folderAdapter.addChildToGroup(groupName, childName)
        Toast.makeText(context, "'$childName' 폴더가 추가되었습니다.", Toast.LENGTH_SHORT).show()
    }
}