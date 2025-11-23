package com.minyook.overnight.ui.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.minyook.overnight.databinding.FragmentMypageBinding // ★ 바인딩 클래스 Import
// import com.minyook.overnight.ui.login.LoginActivity // 로그인 화면이 있다면 주석 해제
// import com.minyook.overnight.ui.file.SubjectFolderActivity // 폴더 목록 화면 Import (곧 만들 예정)

class MyPageFragment : Fragment() {

    // 1. 뷰 바인딩 설정 (XML의 버튼들을 코드로 가져오기 위함)
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // fragment_mypage.xml과 연결
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        // 2. [전체 노트 보기] 카드 클릭 이벤트
        binding.cardAllNotes.setOnClickListener {
            // ★ 여기서 빨간 줄이 뜨면 정상입니다! (아직 SubjectFolderActivity를 안 만들었기 때문)
            // 다음 단계에서 만들면 사라집니다.
            val intent = Intent(requireContext(), SubjectFolderActivity::class.java)
            startActivity(intent)
        }

        // 3. [로그아웃] 버튼 클릭 이벤트
        binding.btnLogout.setOnClickListener {
            // 로그아웃 로직 (예: 토스트 메시지 띄우기)
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            // 로그인 화면으로 이동하는 코드 (LoginActivity가 있다면 주석을 푸세요)
            /*
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 뒤로가기 방지
            startActivity(intent)
            */
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}