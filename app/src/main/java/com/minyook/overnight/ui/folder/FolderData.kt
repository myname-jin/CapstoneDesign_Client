// ui.folder/FolderData.kt

import java.util.UUID

// Sealed Class: 그룹과 하위 항목을 하나의 타입으로 관리
sealed class FolderItem {
    // 1. 그룹 항목 (예: 전체 노트, 공유 받은 노트)
    data class Group(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        var isExpanded: Boolean = false, // 펼침 상태
        val children: MutableList<Child> // 하위 노트/폴더 목록
    ) : FolderItem()

    // 2. 하위 항목 (예: 글로벌, 기본 폴더)
    data class Child(
        val parentId: String,
        val id: String = UUID.randomUUID().toString(),
        val name: String
    ) : FolderItem()
}