package com.minyook.overnight.ui.FirstScrean

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.minyook.overnight.R
import com.minyook.overnight.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    // ViewBinding 설정
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupSignUpPromptText() // HTML 텍스트 설정
    }

    private fun setupClickListeners() {
        // 1. 로그인 버튼
        binding.btnLogin.setOnClickListener {
            // TODO: 입력값 유효성 검사
            // TODO: ViewModel을 통해 실제 로그인 로직 호출
            Toast.makeText(requireContext(), "로그인 시도", Toast.LENGTH_SHORT).show()
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
            // Navigation Component를 사용해 SignUpFragment로 이동
            // TODO: Navigation Graph에 정의할 action ID로 변경 필요
            // 이 ID는 다음 단계에서 만들 auth_nav_graph.xml에 정의됩니다.
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        // 5. 비밀번호 찾기
        binding.tvForgotPassword.setOnClickListener {
            // TODO: 비밀번호 찾기 화면/다이얼로그 구현
            Toast.makeText(requireContext(), "비밀번호 찾기 클릭", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * "계정이 없으신가요? <b>회원가입</b>" 텍스트를 HTML로 변환하여 설정
     */
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