package com.minyook.overnight.ui.FirstScrean

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log // Firebase 로그용 import 추가
import com.google.firebase.firestore.ktx.firestore // Firebase import 추가
import com.google.firebase.ktx.Firebase // Firebase import 추가
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.minyook.overnight.R
import com.minyook.overnight.databinding.FragmentSignUpBinding
import java.util.Calendar
import com.google.firebase.auth.FirebaseAuth // 🔑 Auth import 추가

class SignUpFragment : Fragment() {

    // --- ViewBinding 설정 (LoginFragment와 동일) ---
    // 1. _binding: 뷰가 파괴될 때 null로 초기화되어야 하므로 '?'(nullable)로 선언
    private var _binding: FragmentSignUpBinding? = null
    // 2. binding: 뷰가 살아있는 동안에는 _binding을 null이 아니라고 보장(!!)하고 편하게 사용
    private val binding get() = _binding!!
    // ------------------------------------------

    // Firebase Authentication 객체 선언
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Auth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰(UI)가 성공적으로 만들어진 직후, UI 관련 설정들을 여기서 수행
        setupClickListeners()  // 1. 각종 버튼 클릭 리스너 설정
        setupLoginPromptText() // 2. '로그인' 텍스트에 스타일(굵게) 적용
    }

    /**
     * 화면에 있는 여러 버튼/입력창들의 클릭 이벤트를 한 곳에서 관리합니다.
     */
    private fun setupClickListeners() {

        // 1. 툴바 뒤로가기 버튼
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 2. 생년월일 입력창 클릭
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }
        // 2-1. 생년월일 입력창 오른쪽의 달력 아이콘을 클릭했을 때
        binding.tilBirthdate.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        // 3. 회원가입 버튼 클릭 시 (Firebase 인증 및 DB 저장 시작)
        binding.btnSubmit.setOnClickListener {
            registerUserWithFirebase()
        }

        // 4. 로그인 프롬프트 텍스트 클릭 시 (로그인 화면으로 돌아가기)
        binding.tvLoginPrompt.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * 사용자가 입력한 정보로 Firebase에 회원가입을 시도하는 핵심 함수입니다.
     */
    private fun registerUserWithFirebase() {
        // 1. 사용자가 입력한 값을 모두 가져옴. (trim()으로 공백 제거)
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val date = binding.etDate.text?.toString()?.trim() ?: ""

        // 2. (유효성 검사 1) 필수 항목이 비어있는지 확인합니다.
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "이메일, 비밀번호, 이름은 필수입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. (유효성 검사 2) Firebase Auth의 기본 정책(비밀번호 6자리 이상) 확인
        if (password.length < 6) {
            Toast.makeText(requireContext(), "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_LONG).show()
            return
        }


        // 버튼 중복 클릭 방지
        binding.btnSubmit.isEnabled = false

        // 4. Firebase Auth에 이 이메일과 비밀번호로 계정 생성 요청
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->  // 5. 요청 결과(task)를 비동기적으로 받음
                // 6. (결과 도착) 성공이든 실패든 버튼을 다시 활성화
                binding.btnSubmit.isEnabled = true
                if (task.isSuccessful) {
                    // --- 7. 인증(Auth) 성공! ---
                    // (로그인/비번을 관리하는 Auth 서버에 등록 성공)
                    val user = auth.currentUser  // 방금 생성된 사용자 정보를 가져옴

                    if (user != null) {
                        // 8. Auth에 등록 성공했으니, Firestore DB에 이름, 전화번호 추가 정보 저장
                        saveUserDetailToFirestore(user.uid, name, email, phone, date)
                    }

                } else {
                    // --- 9. 인증(Auth) 실패 ---
                    // (예: 이메일 형식이 아예 틀림, 이미 가입된 이메일 주소 등)
                    Log.w("SignUp", "인증 실패", task.exception)  // 실패 원인을 로그에 기록
                    Toast.makeText(requireContext(), "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Authentication(인증)에 성공한 유저의 추가 정보(이름, 폰, 생일 등)를
     * Firestore 데이터베이스(DB)에 저장합니다.
     */
    private fun saveUserDetailToFirestore(uid: String, name: String, email: String, phone: String, date: String) {
        // 1. Firestore 데이터베이스 인스턴스를 가져옵니다.
        val db = Firebase.firestore

        // 2. DB에 저장할 데이터를 "Key-Value" 형태(맵)로 만듭니다.
        //    (여기서 "name", "email" 등은 DB의 필드명(컬럼명)이 됩니다.)
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "birth" to date,
            "user_docid" to uid // Authentication UID를 Firestore에 저장
        )

        // 3. "user"라는 이름의 컬렉션에
        //    방금 받은 Auth의 UID를 문서(Document) ID로 사용하여 데이터를 저장
        db.collection("user").document(uid).set(userData)
            .addOnSuccessListener {  // 4. DB 저장 성공 시
                Toast.makeText(requireContext(), "정상적으로 가입이 완료되었습니다. ", Toast.LENGTH_LONG).show()
                Log.d("SignUp", "DB 저장 성공. UID: $uid")

                // 이전 화면(로그인 화면)으로 돌아갑니다.
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->  // 6. DB 저장 실패 시 (Auth는 성공했지만 DB만 실패한 경우)
                Toast.makeText(requireContext(), "DB 저장 실패: ${e.message}", Toast.LENGTH_LONG).show()
                Log.w("SignUp", "Firestore 저장 오류", e)
                // (이 경우, Auth에는 계정이 생성되었지만 DB에는 정보가 없는 '유령 계정'이 될 수 있으므로
                //  실제 서비스에서는 Auth 계정을 다시 삭제하는 등의 예외 처리가 필요할 수 있습니다.)
            }
    }

    /**
     * "이미 계정이 있으신가요? <b>로그인</b>" 텍스트를 HTML로 변환하여 설정
     */
    @Suppress("DEPRECATION")
    private fun setupLoginPromptText() {
        val text = getString(R.string.prompt_login)
        binding.tvLoginPrompt.text = Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY)
    }

    /**
     * DatePicker 다이얼로그를 표시
     */
    private fun showDatePickerDialog() {
        // 1. 현재 날짜를 기준으로 캘린더 객체를 가져옵니다.
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // 2. DatePickerDialog 객체를 생성합니다. (날짜가 선택되었을 때의 동작 포함)
        val datePickerDialog = DatePickerDialog(
            requireContext(),  // 팝업이 뜰 부모 컨텍스트
            { _, selectedYear, selectedMonth, selectedDay ->  // 3. 날짜 선택 완료 시 실행될 람다 함수
                // (월(month)은 0부터 시작하므로 +1 해줘야 함)
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                // 4. 선택된 날짜를 EditText에 텍스트로 설정
                binding.etDate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        // 5. (선택 옵션) 선택할 수 있는 최대 날짜를 '오늘'로 설정 (미래 날짜 선택 방지)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        // 6. 팝업을 화면에 표시
        datePickerDialog.show()
    }

    // 메모리 누수 방지를 위해 onDestroyView에서 바인딩 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}