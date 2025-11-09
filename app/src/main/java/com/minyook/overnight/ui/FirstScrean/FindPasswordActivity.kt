package com.minyook.overnight.ui.FirstScrean // ⭐️ LoginFragment와 같은 패키지인지 확인

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // 1. Firebase Auth 임포트
import com.minyook.overnight.databinding.ActivityFindPasswordBinding // ⭐️ 이 부분 이름이 다를 수 있음

class FindPasswordActivity : AppCompatActivity() {

    // --- ViewBinding 설정 ---
    // (이름은 activity_find_password.xml 파일에 따라 자동으로 'ActivityFindPasswordBinding'이 됨)
    private lateinit var binding: ActivityFindPasswordBinding

    // Firebase 인증(Auth) 기능을 사용하기 위한 객체
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. ViewBinding 초기화 (XML 레이아웃을 실제 뷰 객체로 만듦)
        binding = ActivityFindPasswordBinding.inflate(layoutInflater)
        // 2. 만들어진 뷰의 최상위(root) 레이아웃을 화면에 표시
        setContentView(binding.root)

        // 3. Firebase Auth 인스턴스를 초기
        auth = FirebaseAuth.getInstance()

        // --- 클릭 리스너 설정 ---

        // 1. '로그인으로 돌아가기' 텍스트 클릭 시
        binding.tvBackToLogin.setOnClickListener {
            finish() // 현재 Activity를 종료합니다. (자동으로 이전 화면으로 돌아감)
        }

        // 2. '재설정 링크 전송' 버튼 클릭 시
        binding.btnSendLink.setOnClickListener {
            // 2-1. 입력한 이메일 값을 가져오기. (공백 제거)
            val email = binding.etFindEmail.text.toString().trim()

            // 2-2. (유효성 검사) 이메일이 비어있는지 확인
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일 주소를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2-3. (중복 클릭 방지) 요청을 보내는 동안 버튼을 비활성화
            binding.btnSendLink.isEnabled = false

            // 2-4. (⭐️ 1단계 확인) Firebase에 "이 이메일로 가입된 계정이 있나요?" 라고 먼저 물어봄
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->  // 2-5. 확인 결과(task)를 비동기적으로 받음
                    if (task.isSuccessful) {
                        // 2-6. 확인 성공. 응답(signInMethods)을 꺼내봄
                        val signInMethods = task.result?.signInMethods

                        if (signInMethods.isNullOrEmpty()) {
                            // --- 2-7. 가입된 계정이 없음 (목록이 비어있음) ---
                            Toast.makeText(this, "등록되지 않은 이메일 주소입니다.", Toast.LENGTH_LONG).show()
                            binding.btnSendLink.isEnabled = true // 버튼 다시 활성화
                        } else {
                            // --- 2-8. 가입된 계정이 있음 (목록이 비어있지 않음) ---
                            // (⭐️ 2단계 전송) Firebase에 재설정 이메일 보내달라고 요청
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { sendTask ->  // 2-9. 전송 결과(sendTask)를 받음
                                    if (sendTask.isSuccessful) {
                                        // ⭐️ 이메일 전송 성공
                                        Toast.makeText(this, "비밀번호 재설정 이메일을 보냈습니다. 이메일함을 확인해주세요.", Toast.LENGTH_LONG).show()
                                        // 성공 시 로그인 화면으로 돌려보내기
                                        finish()
                                    } else {
                                        // ⭐️ 이메일 전송 실패 (예: Firebase 서버 내부 오류)
                                        Toast.makeText(this, "이메일 전송에 실패했습니다: ${sendTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                    // ⭐️ 이메일 전송 작업이 끝나면 버튼을 다시 활성화
                                    binding.btnSendLink.isEnabled = true
                                }
                        }
                    } else {
                        // --- 2-10. '1단계 확인' 자체가 실패한 경우 ---
                        Toast.makeText(this, "오류가 발생했습니다: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        binding.btnSendLink.isEnabled = true // 버튼 다시 활성화
                    }
                }
        }
    }
}