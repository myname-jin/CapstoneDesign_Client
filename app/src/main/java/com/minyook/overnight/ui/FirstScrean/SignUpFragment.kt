package com.minyook.overnight.ui.FirstScrean

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.minyook.overnight.R
import com.minyook.overnight.databinding.FragmentSignUpBinding
import java.util.Calendar

class SignUpFragment : Fragment() {

    // ViewBinding 설정
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

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
            // NavController를 사용해 이전 화면(로그인 프래그먼트)으로 돌아감
            findNavController().popBackStack()
        }

        // 2. 생년월일 입력 필드 클릭 시
        binding.etBirthdate.setOnClickListener {
            showDatePickerDialog()
        }

        // 3. 생년월일 캘린더 아이콘 클릭 시
        binding.tilBirthdate.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        // 4. 회원가입 버튼 클릭 시
        binding.btnSignUp.setOnClickListener {
            // TODO: 입력값 유효성 검사 (Validation) 로직 추가
            // TODO: ViewModel을 통해 실제 회원가입 로직 호출
            Toast.makeText(requireContext(), "회원가입 시도", Toast.LENGTH_SHORT).show()
        }

        // 5. 로그인 프롬프트 텍스트 클릭 시
        binding.tvLoginPrompt.setOnClickListener {
            // TODO: Navigation Graph에 정의된 action ID로 변경해야 함
            // 예: findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)

            // 우선 뒤로가기(popBackStack)로 로그인 화면으로 돌아가게 처리
            findNavController().popBackStack()
        }
    }

    /**
     * "이미 계정이 있으신가요? <b>로그인</b>" 텍스트를 HTML로 변환하여 설정
     */
    private fun setupLoginPromptText() {
        val text = getString(R.string.prompt_login)
        binding.tvLoginPrompt.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
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
                // 날짜 포맷 (예: 18/03/2024)
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etBirthdate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        // 미래 날짜는 선택 못하게 설정 (선택 사항)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    // 메모리 누수 방지를 위해 onDestroyView에서 바인딩 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}