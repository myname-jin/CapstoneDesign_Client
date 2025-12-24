# 파일구조
```bash
com.minyook.overnight
 ├── ui
 │   ├── activity
 │   │   ├── OvernightActivity.kt          # 메인 네비게이션 컨트롤러
 │   │   ├── PresentationInfoActivity.kt   # 발표 정보 입력
 │   │   ├── UploadActivity.kt             # 영상 업로드/서버 요청
 │   │   ├── AnalysisProgressActivity.kt   # 분석 상태 폴링
 │   │   ├── AnalysisResultActivity.kt     # 결과 화면
 │   │   ├── ScriptChatActivity.kt         # 발표 대본 챗봇 UI
 │   │   └── GuideActivity.kt              # 서비스 이용 가이드
 │   ├── fragment
 │   │   ├── HomeFragment.kt
 │   │   ├── FolderFragment.kt
 │   │   └── MyPageFragment.kt
 │   ├── adapter
 │   │   ├── CriteriaListAdapter.kt        # 채점 기준 RecyclerView
 │   │   └── ChatAdapter.kt                # 챗봇 메시지 RecyclerView
 │   └── customview
 │       └── MultiSegmentDonutChart.kt     # 커스텀 도넛 차트 뷰
 ├── data
 │   └── model
 │       ├── User.kt
 │       ├── PresentationFile.kt
 │       ├── CriterionResult.kt
 │       └── AnalysisResult.kt
 ├── network
 │   ├── RetrofitClient.kt               # 백엔드 서버 통신 설정
 │   └── ApiService.kt                   # API 인터페이스 정의 (/analyze, /status, /chat)
 └── utils
     ├── FileUtils.kt                    # 로컬 파일 관리
     └── PdfUtils.kt                     # 결과 리포트 PDF 저장
