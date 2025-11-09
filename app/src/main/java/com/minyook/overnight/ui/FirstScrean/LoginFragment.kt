package com.minyook.overnight.ui.FirstScrean

import android.content.Context
import android.content.Intent // ⭐️ Intent 사용을 위한 import
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.minyook.overnight.R
import com.minyook.overnight.databinding.FragmentLoginBinding
import com.minyook.overnight.ui.mainscrean.OvernightActivity // ⭐️ OvernightActivity 임포트

class LoginFragment : Fragment() {

    // SharedPreferences 설정을 위한 상수
    private val PREFS_FILE_NAME = "OvernightAppPrefs"
    private val USER_UID_KEY = "user_uid"

    // Navigation Action ID (회원가입 액션은 유지)
    // ⚠️ ACTION_TO_HOME_FRAGMENT는 더 이상 사용하지 않으므로 제거합니다.
    private val ACTION_TO_SIGN_UP = R.id.action_loginFragment_to_signUpFragment

    // ViewBinding 설정
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    // Firebase Authentication 객체 선언
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase Auth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        // checkLoginStatusAndNavigate() (로그인 상태인지 세션 확인 : 로그인 상태면 로그인 안하고 바로 메인화면)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupSignUpPromptText() // HTML 텍스트 설정
    }

    private fun checkLoginStatusAndNavigate() {
        // SharedPreferences에서 UID를 가져와 세션이 있는지 확인
        val sharedPrefs = requireActivity().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val userUid = sharedPrefs.getString(USER_UID_KEY, null)

        // Firebase Auth의 현재 사용자 객체가 null이 아니고 UID가 저장되어 있다면 Home으로 이동
        if (auth.currentUser != null && userUid != null) {
            Log.d("Auth", "자동 로그인 성공. UID: $userUid")
            // ⭐️ 프래그먼트 이동 대신 액티비티 이동 함수 호출
            navigateToOvernightActivity()
        }
    }

    private fun setupClickListeners() {
        // 1. 로그인 버튼
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // 2. Google 로그인 버튼
        binding.btnGoogleLogin.setOnClickListener {
            // TODO: Google 로그인 로직 구현
            Toast.makeText(requireContext(), "Google 로그인 시도", Toast.LENGTH_SHORT).show()
        }

        // 3. Microsoft 로그인 버튼
        binding.btnMicrosoftLogin.setOnClickListener {
            // TODO: Microsoft 로그인 로직 구현
            Toast.makeText(requireContext(), "Microsoft 로그인 시도", Toast.LENGTH_SHORT).show()
        }

        // 4. 회원가입 프롬프트 텍스트 클릭
        binding.tvSignupPrompt.setOnClickListener {
            findNavController().navigate(ACTION_TO_SIGN_UP)
        }

        // 5. 비밀번호 찾기
        binding.tvForgotPassword.setOnClickListener {
            // TODO: 비밀번호 찾기 화면/다이얼로그 구현
            Toast.makeText(requireContext(), "비밀번호 찾기 클릭", Toast.LENGTH_SHORT).show()
        }
    }


    private fun performLogin() {
        // 1. 입력값 가져오기
        val email = binding.etEmail.text?.toString()?.trim()
        val password = binding.etPassword.text?.toString()?.trim()

        // 2. 유효성 검사 (간단)
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 버튼 비활성화 (중복 클릭 방지)
        binding.btnLogin.isEnabled = false

        // 4. Firebase 로그인 실행
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.btnLogin.isEnabled = true // 버튼 다시 활성화

                if (task.isSuccessful) {
                    // 로그인 성공
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener // UID 가져오기

                    // 1. SharedPreferences에 사용자 UID 저장 (세션 기억)
                    saveUserUid(uid)

                    Toast.makeText(requireContext(), "로그인 성공: ${user.email}", Toast.LENGTH_LONG).show()
                    Log.d("Auth", "로그인 성공, UID 저장됨: $uid")

                    // 2. ⭐️ HomeFragment로 이동하는 대신 OvernightActivity로 이동
                    navigateToOvernightActivity()

                } else {
                    // 로그인 실패
                    Log.w("Auth", "로그인 실패", task.exception)
                    Toast.makeText(requireContext(), "로그인 실패: 아이디/비밀번호를 확인하세요.", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * 사용자 고유 ID(UID)를 SharedPreferences에 저장하여 세션처럼 유지합니다.
     */
    private fun saveUserUid(uid: String) {
        val sharedPrefs = requireActivity().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(USER_UID_KEY, uid)
            .apply() // 비동기적으로 저장
    }

    /**
     * OvernightActivity로 이동하고 현재 액티비티(로그인 화면)를 종료합니다.
     * 이 함수가 findNavController().navigate(ACTION_TO_HOME_FRAGMENT)를 대체합니다.
     */
    private fun navigateToOvernightActivity() {
        // OvernightActivity를 실행하기 위한 Intent 생성
        val intent = Intent(requireContext(), OvernightActivity::class.java)

        // Activity 스택을 정리하여 뒤로 가기 버튼을 눌러도 로그인 화면으로 돌아가지 않도록 합니다.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        // 현재 프래그먼트를 호스팅하는 액티비티를 종료합니다.
        requireActivity().finish()
    }

    /**
     * "계정이 없으신가요? <b>회원가입</b>" 텍스트를 HTML로 변환하여 설정
     */
    @Suppress("DEPRECATION")
    private fun setupSignUpPromptText() {
        val text = getString(R.string.prompt_signup)
        binding.tvSignupPrompt.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    }

    // 메모리 누수 방지를 위해 onDestroyView에서 바인딩 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}