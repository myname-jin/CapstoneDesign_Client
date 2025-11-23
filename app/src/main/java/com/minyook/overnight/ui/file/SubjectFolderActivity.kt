package com.minyook.overnight.ui.file

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.minyook.overnight.R
import com.minyook.overnight.data.model.PresentationFile
import com.minyook.overnight.data.model.SubjectFolder
import com.minyook.overnight.databinding.ActivitySubjectFolderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubjectFolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectFolderBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val folderList = ArrayList<SubjectFolder>()
    private val fileList = ArrayList<PresentationFile>()
    private var selectedFolderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        fetchFolders()

        binding.etFolderPath.setOnClickListener {
            if (folderList.isEmpty()) {
                Toast.makeText(this, "폴더가 없거나 로딩 중입니다. 다시 시도합니다.", Toast.LENGTH_SHORT).show()
                fetchFolders()
            } else {
                showFolderSelectionSheet()
            }
        }

        binding.etFilePath.setOnClickListener {
            if (selectedFolderId == null) {
                Toast.makeText(this, "먼저 폴더를 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else if (fileList.isEmpty()) {
                Toast.makeText(this, "선택된 폴더에 파일이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                showFileSelectionSheet()
            }
        }
    }

    private fun showFolderSelectionSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_selection_list, null)
        view.findViewById<TextView>(R.id.tv_selection_title).text = "과목 폴더 선택"
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_selection_list)

        val adapter = SubjectFolderAdapter(folderList) { selectedFolder ->
            binding.etFolderPath.setText(selectedFolder.title)
            selectedFolderId = selectedFolder.id
            binding.layoutFileInput.visibility = View.VISIBLE
            binding.etFilePath.setText("파일을 선택해주세요")
            fetchPresentations(selectedFolder.id)
            dialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showFileSelectionSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_selection_list, null)
        view.findViewById<TextView>(R.id.tv_selection_title).text = "발표 파일 선택"
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_selection_list)

        val adapter = FileListAdapter(fileList) { selectedFile ->
            binding.etFilePath.setText(selectedFile.title)
            dialog.dismiss()
            moveToAnalysisResult(selectedFile)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        dialog.setContentView(view)
        dialog.show()
    }

    private fun moveToAnalysisResult(file: PresentationFile) {
        val intent = Intent(this, AnalysisResultActivity::class.java)
        intent.putExtra("FILE_TITLE", file.title)
        intent.putExtra("FILE_SCORE", file.score)
        intent.putExtra("FILE_DATE", file.date)
        intent.putExtra("FILE_SUMMARY", file.summary)
        startActivity(intent)
    }

    /**
     * ★★★ 최종 수정: FolderFragment와 동일한 경로 및 필드명 사용 ★★★
     * 경로: user/{uid}/folders
     * 필드: name, isDeleted, created_at
     */
    private fun fetchFolders() {
        val uid = auth.currentUser?.uid ?: return

        // 경로 변경: contents -> user/{uid}/folders
        db.collection("user").document(uid).collection("folders")
            .orderBy("created_at", Query.Direction.DESCENDING) // 'created_at' 기준으로 정렬
            .get()
            .addOnSuccessListener { result ->
                folderList.clear()
                for (doc in result) {
                    // 필드명 변경: contentName -> name
                    val name = doc.getString("name") ?: "제목 없음"
                    val isDeleted = doc.getBoolean("isDeleted") ?: false

                    if (!isDeleted) {
                        // 타임스탬프 필드명 변경: createdAt -> created_at
                        // 타입 안전성 확보: Timestamp와 Long 모두 시도
                        val timestamp = try {
                            doc.getTimestamp("created_at")
                        } catch (e: Exception) {
                            val longValue = doc.getLong("created_at")
                            if (longValue != null) com.google.firebase.Timestamp(Date(longValue)) else null
                        }

                        val dateStr = getDateString(timestamp)

                        folderList.add(SubjectFolder(doc.id, name, dateStr))
                    }
                }
                if (folderList.isEmpty()) {
                    Log.d("FolderDebug", "No folders loaded for UID: $uid")
                    Toast.makeText(this, "등록된 폴더가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // 이 코드가 실행될 경우, user/{uid}/folders 경로에 대한 인덱스가 없는 것임.
                Log.e("FolderDebug", "Error loading folders: ${e.message}")
                Toast.makeText(this, "폴더 로드에 실패했습니다. (인덱스/네트워크 확인)", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * 파일 목록 로드 (Presentations 컬렉션 사용) - 'contentId'가 필터링 조건
     * 폴더 ID(user/{uid}/folders의 ID)를 contentId로 사용하여 presentations 문서를 찾음.
     */
    private fun fetchPresentations(contentId: String) {
        fileList.clear()

        // presentations 컬렉션에서 contentId가 일치하는 문서만 쿼리
        db.collection("presentations")
            .whereEqualTo("contentId", contentId)
            .orderBy("gradeAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val title = doc.getString("topicName") ?: "제목 없음"
                    val score = doc.get("scores")?.let { scores ->
                        if (scores is List<*>) {
                            (scores.firstOrNull() as? Map<*, *>)?.get("scoreValue") as? Long
                        } else {
                            null
                        }
                    }?.toInt() ?: 0

                    val summary = doc.getString("overallFeedback") ?: "요약 정보 없음"
                    val dateStr = getDateString(doc.getTimestamp("gradeAt"))

                    fileList.add(PresentationFile(doc.id, title, dateStr, score, summary))
                }
                if (fileList.isEmpty()) {
                    Toast.makeText(this, "선택된 폴더에 분석된 파일이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FileDebug", "Error loading presentations: ${e.message}")
                Toast.makeText(this, "파일 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getDateString(timestamp: com.google.firebase.Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it)
        } ?: "-"
    }
}