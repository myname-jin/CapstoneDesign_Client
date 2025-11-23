package com.minyook.overnight.ui.mainscrean

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.minyook.overnight.R
import com.minyook.overnight.ui.file.UploadActivity

class PresentationInfoActivity : AppCompatActivity(),
    FolderSelectionBottomSheet.OnFolderSelectedListener {

    private lateinit var itemsContainer: LinearLayout
    private lateinit var addItemButton: Button
    private lateinit var startButton: Button
    private lateinit var folderPathEditText: TextInputEditText
    private lateinit var etTeamInfo: TextInputEditText
    private lateinit var etTopicName: TextInputEditText

    private var itemCounter = 0

    // Firestore 관련 변수
    private lateinit var db: FirebaseFirestore
    private var selectedFolderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation_info)

        // ★ [추가] 시스템 UI 숨기기 (내비게이션 바 숨김)
        hideSystemUI()

        // 1. Firestore 초기화
        db = FirebaseFirestore.getInstance()

        // 2. 뷰 바인딩
        itemsContainer = findViewById(R.id.itemsContainer)
        addItemButton = findViewById(R.id.addItemButton)
        startButton = findViewById(R.id.startButton)
        folderPathEditText = findViewById(R.id.edittext_folder_path)
        etTeamInfo = findViewById(R.id.edittext_team_info)
        etTopicName = findViewById(R.id.edittext_topic_info)

        // 3. 폴더 선택 팝업
        folderPathEditText.setOnClickListener {
            val bottomSheet = FolderSelectionBottomSheet()
            bottomSheet.show(supportFragmentManager, FolderSelectionBottomSheet.TAG)
        }

        // 4. 기준 항목 추가 버튼
        addItemButton.setOnClickListener {
            if (itemsContainer.childCount < 5) {
                addNewItemCard()
            } else {
                Toast.makeText(this, "항목은 최대 5개까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. 저장 및 시작 버튼
        startButton.setOnClickListener {
            saveTopicToFirestore()
        }

        val btnBack = findViewById<android.widget.ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // 현재 액티비티 종료 -> 이전 화면(FolderFragment)으로 복귀
        }

        // 초기 항목 1개 추가
        addNewItemCard()
    }

    // ★ [추가] 화면이 다시 보일 때도 숨김 모드 유지
    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    // ★ [핵심 기능] 내비게이션 바와 상태 바 숨기기
    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // 스와이프 시 잠깐 나타났다가 사라지게 설정
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // 상단 상태바 & 하단 내비게이션바 모두 숨김
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 채점 기준 항목(Card)을 UI에 동적으로 추가
     */
    private fun addNewItemCard() {
        itemCounter++

        val inflater = LayoutInflater.from(this)
        val itemCardView = inflater.inflate(
            R.layout.item_criterion,
            itemsContainer,
            false
        )

        val deleteButton: ImageButton = itemCardView.findViewById(R.id.button_delete_item)



        deleteButton.setOnClickListener {
            itemsContainer.removeView(itemCardView)
            itemCounter--
        }

        itemsContainer.addView(itemCardView)
    }

    override fun onFolderSelected(folderId: String, folderName: String) {
        selectedFolderId = folderId
        folderPathEditText.setText(folderName)
    }

    private fun saveTopicToFirestore() {
        if (selectedFolderId == null) {
            Toast.makeText(this, "폴더(과목)를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val teamInfo = etTeamInfo.text.toString().trim()
        val topicName = etTopicName.text.toString().trim()

        if (teamInfo.isEmpty() || topicName.isEmpty()) {
            Toast.makeText(this, "팀 정보와 발표 주제를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val standardsList = mutableListOf<HashMap<String, Any>>()
        var totalScore = 0

        for (i in 0 until itemsContainer.childCount) {
            val view = itemsContainer.getChildAt(i)
            val etName = view.findViewById<TextInputEditText>(R.id.edittext_item_name)
            val etContent = view.findViewById<TextInputEditText>(R.id.edittext_item_content)
            val etScore = view.findViewById<TextInputEditText>(R.id.edittext_item_score)

            val name = etName.text.toString().trim()
            val detail = etContent.text.toString().trim()
            val scoreStr = etScore.text.toString().trim()

            if (name.isEmpty() || scoreStr.isEmpty()) {
                Toast.makeText(this, "모든 평가 항목의 이름과 배점을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            val score = scoreStr.toIntOrNull() ?: 0
            totalScore += score

            val standardMap = hashMapOf <String, Any> (
                "standardName" to name,
                "standardDetail" to detail,
                "standardScore" to score
            )
            standardsList.add(standardMap)
        }

        if (totalScore != 100) {
            Toast.makeText(this, "배점의 총합은 100점이 되어야 합니다. (현재: ${totalScore}점)", Toast.LENGTH_LONG).show()
            return
        }

        val topicData = hashMapOf(
            "contentId" to selectedFolderId,
            "topicName" to topicName,
            "teamInfo" to teamInfo,
            "standards" to standardsList,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        startButton.isEnabled = false

        db.collection("contents").document(selectedFolderId!!)
            .collection("topics")
            .add(topicData)
            .addOnSuccessListener { documentReference ->
                val newTopicId = documentReference.id
                Toast.makeText(this, "발표 주제가 저장되었습니다.", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, UploadActivity::class.java)
                intent.putExtra("contentId", selectedFolderId)
                intent.putExtra("topicId", newTopicId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                startButton.isEnabled = true
                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}