package com.minyook.overnight.ui.mainscrean

import android.content.Intent // 👈 [수정됨] Intent 임포트
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast // 👈 [수정됨] Toast 임포트
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.minyook.overnight.R // 👈 R파일 임포트 (프로젝트 패키지명에 맞게)
import com.minyook.overnight.ui.file.UploadActivity

class PresentationInfoActivity : AppCompatActivity() {

    // 1. 뷰들을 나중에 참조할 수 있게 클래스 멤버로 선언
    private lateinit var itemsContainer: LinearLayout
    private lateinit var addItemButton: Button
    private lateinit var startButton: Button // 👈 [수정됨] 시작 버튼 변수 추가

    // 2. 추가된 항목의 개수를 세는 카운터
    private var itemCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation_info)

        // 3. 뷰 초기화
        itemsContainer = findViewById(R.id.itemsContainer)
        addItemButton = findViewById(R.id.addItemButton)
        startButton = findViewById(R.id.startButton) // 👈 [수정됨] 시작 버튼 초기화

        // 4. '+ 항목 추가' 버튼 클릭 리스너 설정
        addItemButton.setOnClickListener {

            // 🔴 [수정됨] 5개 제한 로직 추가 🔴
            // 현재 컨테이너에 추가된 뷰(항목 카드)의 개수를 확인합니다.
            if (itemsContainer.childCount < 5) {
                addNewItemCard()
            } else {
                // 5개를 초과하면 토스트 메시지 표시
                Toast.makeText(this, "항목은 최대 5개까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔴 [수정됨] '발표 시작하기' 버튼 클릭 리스너 설정 🔴
        startButton.setOnClickListener {
            // UploadActivity로 이동하는 Intent 생성
            // ⚠️ (주의) UploadActivity.kt 파일이 있어야 합니다.
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

        // 5. 화면이 처음 열릴 때 기본으로 항목 1개를 추가
        addNewItemCard()
    }

    /**
     * 6. 새 항목 카드를 itemsContainer에 추가하는 함수
     */
    private fun addNewItemCard() {
        // 7. 카운터 증가
        itemCounter++

        // 8. LayoutInflater를 사용해 item_criterion.xml을 뷰 객체로 만듦
        val inflater = LayoutInflater.from(this)
        val itemCardView = inflater.inflate(
            R.layout.item_criterion, // 재사용할 카드 레이아웃
            itemsContainer,     // 이 뷰의 부모가 될 컨테이너
            false         // 지금 바로 붙이지 않음 (addView로 붙일 것)
        )

        // 9. 카드 뷰 내부의 UI 요소들을 찾음
        val itemNameEditText: TextInputEditText = itemCardView.findViewById(R.id.edittext_item_name)
        val deleteButton: ImageButton = itemCardView.findViewById(R.id.button_delete_item)

        // 10. 새 항목의 기본 텍스트 설정
        itemNameEditText.setText("항목 $itemCounter")

        // 11. 삭제(X) 버튼 클릭 리스너 설정
        deleteButton.setOnClickListener {
            // itemsContainer에서 이 카드 뷰(itemCardView)를 제거
            itemsContainer.removeView(itemCardView)
        }

        // 12. 완성된 카드 뷰를 컨테이너(LinearLayout)에 추가
        itemsContainer.addView(itemCardView)
    }
}