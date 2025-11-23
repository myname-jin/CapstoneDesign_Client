package com.minyook.overnight.ui.folder

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minyook.overnight.R
import com.minyook.overnight.databinding.DialogAddChildBinding
import com.minyook.overnight.databinding.FragmentFolderBinding
import com.minyook.overnight.ui.mainscrean.PresentationInfoActivity

class FolderFragment : Fragment() {

    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ⭐ 사용자님의 기존 어댑터 사용
    private lateinit var folderAdapter: FolderExpandableAdapter

    // 어댑터에 전달할 데이터 리스트 (Group 구조)
    private var groups = mutableListOf<FolderItem.Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSystemUI()

        // 1. 초기 데이터 구조 생성 (전체 노트, 휴지통 그룹 만들기)
        initGroupData()

        // 2. 리사이클러뷰 연결
        setupRecyclerView()

        // 3. Firestore에서 실제 폴더 데이터 불러와서 '전체 노트' 그룹에 자식으로 넣기
        fetchFolders()

        // 4. 버튼 리스너 설정
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    // --- [1] 초기 그룹 데이터 세팅 ---
    private fun initGroupData() {
        groups.clear()
        // '전체 노트' 그룹 생성 (처음엔 자식이 비어있음)
        // isExpanded = true로 해서 처음에 펼쳐져 있게 설정
        groups.add(FolderItem.Group(name = "전체 노트", isExpanded = true, children = mutableListOf()))

        // '휴지통' 그룹 생성
        groups.add(FolderItem.Group(name = "휴지통", children = mutableListOf()))
    }

    // --- [2] 리사이클러뷰 & 어댑터 설정 ---
    private fun setupRecyclerView() {
        // 기존 어댑터 생성자에 맞춰서 콜백 함수들 연결
        folderAdapter = FolderExpandableAdapter(
            data = groups,
            onAddClicked = { groupName ->
                // '전체 노트' 옆의 + 버튼 클릭 시 다이얼로그 띄우기
                showAddFolderDialog()
            },
            onChildClicked = { childName ->
                // 하위 폴더(파일) 클릭 시
                Toast.makeText(context, "$childName 선택됨", Toast.LENGTH_SHORT).show()
            },
            onTrashClicked = {
                // 휴지통 클릭 시
                Toast.makeText(context, "휴지통 클릭됨", Toast.LENGTH_SHORT).show()
            },
            onChildOptionsClicked = { view, childName ->
                // 하위 항목 옵션 버튼 클릭 시 (수정/삭제 등 기능 구현 공간)
                Toast.makeText(context, "$childName 옵션", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerFolderList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = folderAdapter
            // 펼치기/접기 애니메이션 깜빡임 방지
            itemAnimator = null
        }
    }

    // --- [3] Firestore 데이터 불러오기 ---
    private fun fetchFolders() {
        val uid = auth.currentUser?.uid ?: return

        // Firestore 경로: user -> uid -> folders
        db.collection("user").document(uid).collection("folders")
            .orderBy("created_at")
            .get()
            .addOnSuccessListener { documents ->
                // 1. '전체 노트' 그룹 찾기
                val allNotesGroup = groups.find { it.name == "전체 노트" }

                if (allNotesGroup != null) {
                    // 기존 자식 비우기 (중복 추가 방지)
                    allNotesGroup.children.clear()

                    // 2. Firestore 문서를 Child 객체로 변환해서 넣기
                    for (document in documents) {
                        val name = document.getString("name") ?: "이름 없음"
                        val docId = document.id

                        // ⭐ FolderItem.Child 객체 생성 (기존 데이터 클래스 활용)
                        val child = FolderItem.Child(
                            parentId = allNotesGroup.id,
                            id = docId,
                            name = name,
                            isDeleted = false
                        )
                        allNotesGroup.children.add(child)
                    }

                    // 3. 어댑터에게 데이터 변경 알림 -> 화면 갱신
                    folderAdapter.notifyDataChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FolderFragment", "데이터 로드 실패", e)
            }
    }

    // --- [4] Firestore에 새 폴더 저장 ---
    private fun createNewFolder(folderName: String) {
        val uid = auth.currentUser?.uid ?: return

        val folderData = hashMapOf(
            "name" to folderName,
            "created_at" to System.currentTimeMillis()
        )

        db.collection("user").document(uid).collection("folders")
            .add(folderData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "폴더 생성 완료", Toast.LENGTH_SHORT).show()
                // 저장 성공 시 목록 새로고침
                fetchFolders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- [5] 팝업 및 다이얼로그 관련 ---

    private fun setupListeners() {
        // 우측 하단 FAB 버튼 클릭 시 팝업 메뉴
        binding.fabAddFolder.setOnClickListener { view ->
            showAddOptionsMenu(view)
        }
    }

    private fun showAddOptionsMenu(anchorView: View) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_add_options, null)

        val popupWindow = PopupWindow(
            popupView,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // 파일 업로드 버튼
        popupView.findViewById<View>(R.id.btn_upload_file).setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(requireContext(), PresentationInfoActivity::class.java)
            startActivity(intent)
        }

        // 폴더 추가 버튼
        popupView.findViewById<View>(R.id.btn_add_folder_popup).setOnClickListener {
            popupWindow.dismiss()
            showAddFolderDialog()
        }

        // ★ [핵심] 팝업의 크기를 미리 측정합니다.
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWidth = popupView.measuredWidth

        // ★ [위치 계산]
        // xOffset: 팝업의 오른쪽 끝을 버튼의 오른쪽 끝과 맞추거나, 약간 왼쪽으로 이동
        // yOffset: (팝업 높이 + 버튼 높이 + 간격) 만큼 위로(-) 올림
        val xOff = -(popupWidth - anchorView.width) // 버튼과 오른쪽 정렬
        val yOff = -(popupHeight + anchorView.height + 30) // 버튼 위로 30px 띄워서 배치

        // 계산된 위치에 표시
        popupWindow.showAsDropDown(anchorView, xOff, yOff)
    }

    private fun showAddFolderDialog() {
        val dialogBinding = DialogAddChildBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogBinding.root)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnCreate.setOnClickListener {
            val folderName = dialogBinding.etNewChildName.text.toString().trim()
            if (folderName.isNotEmpty()) {
                createNewFolder(folderName)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "폴더 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun hideSystemUI() {
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}