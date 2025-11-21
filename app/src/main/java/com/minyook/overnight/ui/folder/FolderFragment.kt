package com.minyook.overnight.ui.folder

import android.content.Context
import android.content.Intent // 👈 [추가] Intent import
import android.graphics.drawable.BitmapDrawable // 👈 [추가] PopupWindow 배경용 import
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout // 👈 [추가] PopupWindow 내부 뷰 import
import android.widget.PopupWindow // 👈 [추가] PopupWindow import
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.minyook.overnight.R
import com.minyook.overnight.ui.mainscrean.PresentationInfoActivity // 👈 [추가] 이동할 Activity import

/**
 * 폴더 목록을 관리하는 메인 프래그먼트.
 * AddChildDialogFragment.ChildCreationListener 인터페이스를 구현하여
 * 다이얼로그로부터 새 폴더 생성 이벤트를 받습니다.
 */
class FolderFragment : Fragment(), AddChildDialogFragment.ChildCreationListener,
    FolderOptionsBottomSheet.FolderOptionListener, RenameFolderDialogFragment.RenameListener {
    private lateinit var folderAdapter: FolderExpandableAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddFolder: FloatingActionButton

    private lateinit var folderGroupsData: MutableList<FolderItem.Group>
    // -----------------------------------
    // 데이터 초기 설정 (FolderItem.kt 기반)
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
        return inflater.inflate(R.layout.fragment_folder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⭐ 1. 뷰 바인딩 코드가 가장 먼저 실행되어야 합니다.
        recyclerView = view.findViewById(R.id.recycler_folder_list)
        fabAddFolder = view.findViewById(R.id.fab_add_folder)

        // 2. 원본 데이터 저장
        if (!::folderGroupsData.isInitialized) {
            folderGroupsData = getInitialData()
        }

        // 3. 어댑터 초기화 및 콜백 정의
        folderAdapter = FolderExpandableAdapter(
            data = folderGroupsData, // 저장된 데이터 전달
            onAddClicked = ::showAddChildDialog,
            onChildClicked = ::navigateToChildNotes,
            onTrashClicked = ::navigateToTrashList,
            onChildOptionsClicked = ::showChildOptionsBottomSheet
        )

        // 4. 리사이클러뷰 설정 (이제 recyclerView 변수는 초기화된 상태입니다.)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = folderAdapter // ⭐ 이 코드가 이제 성공적으로 실행됩니다.

        // 5. 하단 FAB 리스너 (팝업)
        fabAddFolder.setOnClickListener { anchorView ->
            showAddOptionsPopup(anchorView)
        }
    }

    override fun onResume() {
        super.onResume()
        // 다른 화면(휴지통)에서 데이터 상태(isDeleted)가 바뀌었을 수 있으므로
        // 화면이 다시 보일 때 목록을 갱신합니다.
        if (::folderAdapter.isInitialized) {
            folderAdapter.notifyDataChanged()
        }
    }

    private fun navigateToTrashList() {

        // ⭐ [핵심 수정] 데이터 목록을 Bundle에 담아 TrashNotesFragment로 전달합니다.
        // Arraylist로 변환하여 Bundle에 넣습니다.
        val dataToSend = ArrayList(folderGroupsData)
        val fragment = TrashNotesFragment.newInstance(dataToSend)
        val containerId = (view?.parent as? ViewGroup)?.id ?: R.id.fragment_container

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showChildOptionsBottomSheet(anchorView: View, folderTitle: String) {
        val bottomSheet = FolderOptionsBottomSheet.newInstance(folderTitle)

        // ⭐ 이 Fragment를 타겟으로 설정하여, BottomSheet에서 발생하는 삭제/이름 변경 이벤트를 직접 수신
        bottomSheet.setTargetFragment(this, 0)
        bottomSheet.show(parentFragmentManager, "ChildOptions")
    }

    override fun onFolderDeleted(folderTitle: String) {
        deleteFolderByTitleAndRefresh(folderTitle) // 메모리에서 삭제 로직 수행
    }

    override fun onFolderRenamed(folderTitle: String) {
        // ⭐ [수정] BottomSheet에서 요청이 오면, Rename 다이얼로그를 띄웁니다.
        val dialog = RenameFolderDialogFragment.newInstance(folderTitle)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "RenameDialog")
    }

    override fun onFolderRenamed(oldTitle: String, newTitle: String) {
        // 1. 메모리 데이터(folderGroupsData)를 업데이트합니다.
        var updated = false

        folderGroupsData.forEach { group ->
            val childToRename = group.children.find { it.name == oldTitle }
            if (childToRename != null) {
                childToRename.name = newTitle // 이름 변경
                updated = true
                return@forEach
            }
        }

        if (updated) {
            // 2. Adapter에 데이터가 변경되었음을 알리고 UI를 갱신합니다.
            folderAdapter.notifyDataChanged()
            Toast.makeText(context, "'$oldTitle' 폴더가 '$newTitle'로 변경되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFolderByTitleAndRefresh(folderTitle: String) {
        var markedAsDeleted = false // 변수 이름 변경

        // 원본 데이터(folderGroupsData)에서 항목을 찾습니다.
        folderGroupsData.forEach { group ->
            val childToTrash = group.children.find { it.name == folderTitle }
            if (childToTrash != null) {
                // ⭐ [핵심 수정] 항목을 삭제하는 대신, isDeleted 플래그를 true로 설정합니다.
                childToTrash.isDeleted = true
                markedAsDeleted = true
                return@forEach
            }
        }

        if (markedAsDeleted) {
            // isDeleted 상태가 변경되었으므로 어댑터를 갱신합니다.
            folderAdapter.notifyDataChanged()
            Toast.makeText(context, "'$folderTitle' 폴더가 휴지통으로 이동되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // -----------------------------------
    // 팝업 로직 (HomeFragment에서 이전)
    // -----------------------------------

    /**
     * '추가' 옵션 팝업창을 띄우는 함수 (PopupWindow 사용)
     */
    private fun showAddOptionsPopup(anchorView: View) {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // 1. 팝업 레이아웃 inflate (이전에 만든 popup_add_options.xml 사용)
        val popupView = inflater.inflate(R.layout.popup_add_options, null)

        // 2. PopupWindow 객체 생성
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 3. 팝업 배경 설정 (외부 터치 시 닫히도록)
        popupWindow.setBackgroundDrawable(BitmapDrawable())
        popupWindow.isOutsideTouchable = true

        // 4. 팝업 내부의 뷰 찾기
        val optionRecord: LinearLayout = popupView.findViewById(R.id.option_record)
        val optionFileUpload: LinearLayout = popupView.findViewById(R.id.option_file_upload)

        // 5. "녹화" 클릭
        optionRecord.setOnClickListener {
            Toast.makeText(requireContext(), "녹화 기능 실행 (구현 필요)", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        // 6. "파일 업로드" 클릭 (PresentationInfoActivity로 이동)
        optionFileUpload.setOnClickListener {
            // PresentationInfoActivity로 이동하는 Intent 생성
            // ⚠️ 여기서 PresentationInfoActivity 클래스 경로는 프로젝트 구조에 맞게 수정해야 할 수 있습니다.
            val intent = Intent(requireContext(), PresentationInfoActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }

        // 7. 팝업 위치 계산 (FAB 위쪽으로)
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight

        val yOffset = - (anchorView.height + popupHeight + 16)

        popupWindow.showAsDropDown(anchorView, 0, yOffset)
    }

    // -----------------------------------
    // 다이얼로그 호출 및 화면 이동
    // -----------------------------------

    /**
     * '전체 노트' 그룹의 '+' 버튼을 눌렀을 때 호출됩니다.
     */
    private fun showAddChildDialog(groupName: String) {
        val dialog = AddChildDialogFragment.newInstance(groupName)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "AddChildDialog")
    }

    /**
     * 자식 폴더 항목을 클릭했을 때 호출됩니다.
     */
    private fun navigateToChildNotes(folderTitle: String) {
        val fragment = ChildNotesFragment.newInstance(folderTitle)
        val containerId = (view?.parent as? ViewGroup)?.id ?: R.id.fragment_container

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    // -----------------------------------
    // AddChildDialogFragment.ChildCreationListener 구현
    // -----------------------------------

    /**
     * 다이얼로그에서 '추가' 버튼을 누르면 이 콜백이 실행됩니다.
     */
    override fun onChildCreated(groupName: String, childName: String) {
        folderAdapter.addChildToGroup(groupName, childName)
        Toast.makeText(context, "'$childName' 폴더가 추가되었습니다.", Toast.LENGTH_SHORT).show()
    }
}