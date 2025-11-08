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

    // ViewBinding 설정
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

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

        setupClickListeners()
        setupLoginPromptText() // HTML 텍스트 설정
    }

    private fun setupClickListeners() {

        // 1. 툴바 뒤로가기 버튼
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 2. 생년월일 입력 필드 및 아이콘 클릭 시
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }
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

    private fun registerUserWithFirebase() {
        // 1. 입력값 가져오기
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val date = binding.etDate.text?.toString()?.trim() ?: ""

        // 2. 유효성 검사 (필수 항목 확인)
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "이메일, 비밀번호, 이름은 필수입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 비밀번호 길이 확인 (Firebase 기본 규칙: 6자리 이상)
        if (password.length < 6) {
            Toast.makeText(requireContext(), "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_LONG).show()
            return
        }


        // 버튼 중복 클릭 방지
        binding.btnSubmit.isEnabled = false

        // 4. Firebase Authentication에 계정 생성 (가장 중요!)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.btnSubmit.isEnabled = true // 버튼 활성화

                if (task.isSuccessful) {
                    // 🔑 Authentication 서버 등록 성공!
                    val user = auth.currentUser
                    if (user != null) {
                        // 5. Firestore에 추가 정보 저장
                        saveUserDetailToFirestore(user.uid, name, email, phone, date)
                    }

                } else {
                    // 계정 생성 실패 (예: 이메일 형식이 잘못됨, 이미 존재하는 계정)
                    Log.w("SignUp", "인증 실패", task.exception)
                    Toast.makeText(requireContext(), "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserDetailToFirestore(uid: String, name: String, email: String, phone: String, date: String) {
        val db = Firebase.firestore

        // Firestore에 저장할 데이터 맵 (DB 필드명과 일치)
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "birth" to date,
            "tel" to phone,
            "user_docid" to uid // Authentication UID를 Firestore에 저장
        )

        // "user" 컬렉션에 UID를 문서 ID로 사용하여 저장
        db.collection("user").document(uid).set(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "✅ 회원가입 및 DB 저장 성공!", Toast.LENGTH_LONG).show()
                Log.d("SignUp", "DB 저장 성공. UID: $uid")

                // 로그인 화면으로 돌아가기
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "DB 저장 실패: ${e.message}", Toast.LENGTH_LONG).show()
                Log.w("SignUp", "Firestore 저장 오류", e)
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
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    // 메모리 누수 방지를 위해 onDestroyView에서 바인딩 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}