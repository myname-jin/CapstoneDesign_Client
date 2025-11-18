package com.minyook.overnight.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable // 👈 PopupWindow 배경 처리를 위해 import
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow // 👈 BottomSheet 대신 PopupWindow를 import
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.minyook.overnight.R
import com.minyook.overnight.ui.mainscrean.PresentationInfoActivity

/**
 * HomeFragment:
 * 1. '+' 버튼(FAB) 클릭 이벤트를 처리합니다.
 * 2. 클릭 시 BottomSheet 대신 PopupWindow를 띄웁니다.
 * 3. 팝업창의 "파일 업로드"를 누르면 PresentationInfoActivity로 이동합니다.
 */
class HomeFragment : Fragment() { // 👈 OnOptionClickListener 인터페이스 구현부 삭제

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_home.xml 레이아웃을 이 Fragment에 연결합니다.
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. fragment_home.xml에 있는 fab_add 버튼을 찾습니다.
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add)

        // 2. FAB( + 버튼) 클릭 리스너 설정
        fab.setOnClickListener { anchorView ->
            // 클릭된 뷰(fab)를 기준으로 팝업을 띄우는 함수 호출
            showAddOptionsPopup(anchorView)
        }

        // --- (기존의 RecyclerView 등 다른 UI 요소들 초기화 코드는 여기에 작성) ---
    }

    /**
     * '추가' 옵션 팝업창을 띄우는 함수
     * @param anchorView 팝업창을 띄울 기준이 되는 뷰 (여기서는 FAB)
     */
    private fun showAddOptionsPopup(anchorView: View) {
        // 1. LayoutInflater를 가져옵니다.
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // 2. 팝업으로 띄울 레이아웃(popup_add_options.xml)을 inflate합니다.
        val popupView = inflater.inflate(R.layout.popup_add_options, null)

        // 3. PopupWindow 객체를 생성합니다.
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT, // 너비 (레이아웃에 따름)
            ViewGroup.LayoutParams.WRAP_CONTENT, // 높이 (레이아웃에 따름)
            true // 포커스를 활성화 (바깥쪽 터치 시 닫힘)
        )

        // 4. 바깥쪽을 터치했을 때 닫히게 하려면 배경을 설정해야 합니다.
        popupWindow.setBackgroundDrawable(BitmapDrawable())
        popupWindow.isOutsideTouchable = true

        // 5. 팝업 레이아웃 내부의 뷰들을 찾습니다.
        val optionRecord: LinearLayout = popupView.findViewById(R.id.option_record)
        val optionFileUpload: LinearLayout = popupView.findViewById(R.id.option_file_upload)

        // 6. "녹화" 버튼 클릭 리스너
        optionRecord.setOnClickListener {
            Toast.makeText(requireContext(), "녹화 기능 실행 (구현 필요)", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss() // 팝업 닫기
        }

        // 7. "파일 업로드" 버튼 클릭 리스너
        optionFileUpload.setOnClickListener {
            // PresentationInfoActivity로 이동하는 Intent 생성
            val intent = Intent(requireContext(), PresentationInfoActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss() // 팝업 닫기
        }

        // --- 팝업 위치 계산 (FAB 위쪽으로) ---

        // 8. 팝업 뷰의 정확한 크기를 측정합니다.
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight

        // 9. 팝업을 FAB '위'에 띄우기 위한 y좌표 오프셋을 계산합니다.
        // (음수 값이 위로 이동)
        val yOffset = - (anchorView.height + popupHeight + 16) // 16은 FAB와의 여백

        // 10. 계산된 위치에 팝업창을 보여줍니다.
        popupWindow.showAsDropDown(anchorView, 0, yOffset)
    }

    // (기존 BottomSheet 관련 인터페이스 함수들은 모두 삭제)
}