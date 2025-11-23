package com.minyook.overnight.ui.folder

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minyook.overnight.databinding.DialogAddChildBinding
import com.minyook.overnight.databinding.FragmentFolderBinding

class FolderFragment : Fragment() {

    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ⭐ 사용자님의 기존 어댑터 사용
    private lateinit var folderAdapter: FolderExpandableAdapter

    // 어댑터에 전달할 데이터 리스트
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

        // 1. 초기 데이터 구조 생성 (전체 노트, 휴지통)
        initGroupData()

        // 2. 리사이클러뷰 연결
        setupRecyclerView()

        // 3. Firestore에서 실제 폴더 데이터 불러와서 '전체 노트'에 넣기
        fetchFolders()

        // 4. 버튼 리스너
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    // --- [1] 초기 그룹 데이터 세팅 ---
    private fun initGroupData() {
        groups.clear()
        // '전체 노트' 그룹 생성 (처음엔 비어있음)
        groups.add(FolderItem.Group(name = "전체 노트", isExpanded = true, children = mutableListOf()))
        // '휴지통' 그룹 생성
        groups.add(FolderItem.Group(name = "휴지통", children = mutableListOf()))
    }

    // --- [2] 리사이클러뷰 & 어댑터 설정 ---
    private fun setupRecyclerView() {
        folderAdapter = FolderExpandableAdapter(
            data = groups,
            onAddClicked = { groupName ->
                // + 버튼 클릭 시 다이얼로그 띄우기
                showAddFolderDialog()
            },
            onChildClicked = { childName ->
                Toast.makeText(context, "$childName 클릭됨", Toast.LENGTH_SHORT).show()
            },
            onTrashClicked = {
                Toast.makeText(context, "휴지통 클릭됨", Toast.LENGTH_SHORT).show()
            },
            onChildOptionsClicked = { view, childName ->
                // 옵션 버튼 클릭 시 (수정/삭제 등)
            }
        )

        binding.recyclerFolderList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = folderAdapter
            // 애니메이션이 거슬리면 null 처리
            itemAnimator = null
        }
    }

    // --- [3] Firestore 데이터 불러오기 ---
    private fun fetchFolders() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("user").document(uid).collection("folders")
            .orderBy("created_at")
            .get()
            .addOnSuccessListener { documents ->
                // 1. '전체 노트' 그룹 찾기
                val allNotesGroup = groups.find { it.name == "전체 노트" }

                if (allNotesGroup != null) {
                    // 기존 자식 비우기 (중복 방지)
                    allNotesGroup.children.clear()

                    // 2. Firestore 문서를 Child 객체로 변환해서 넣기
                    for (document in documents) {
                        val name = document.getString("name") ?: "이름 없음"
                        val id = document.id

                        // Child 객체 생성 (FolderItem.kt에 정의된 대로)
                        val child = FolderItem.Child(
                            parentId = allNotesGroup.id,
                            id = id,
                            name = name,
                            isDeleted = false
                        )
                        allNotesGroup.children.add(child)
                    }

                    // 3. 어댑터에 데이터 변경 알림
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
                fetchFolders() // 목록 새로고침
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- [5] 다이얼로그 (디자인 적용됨) ---
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

    private fun setupListeners() {
        // FAB 버튼 클릭 시에도 다이얼로그 띄우기
        binding.fabAddFolder.setOnClickListener {
            showAddFolderDialog()
        }
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