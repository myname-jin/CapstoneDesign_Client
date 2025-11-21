// ui.folder/FolderItem.kt

package com.minyook.overnight.ui.folder

import java.io.Serializable
import java.util.UUID

// Serializable을 상속받아 Fragment 간 데이터 전달이 가능하도록 합니다.
sealed class FolderItem : Serializable {
    // 1. 그룹 항목
    data class Group(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        var isExpanded: Boolean = false,
        val children: MutableList<Child>
    ) : FolderItem()

    // 2. 하위 항목
    data class Child(
        val parentId: String,
        val id: String = UUID.randomUUID().toString(),
        var name: String, // 이름 변경을 위해 var 유지
        // ⭐ [추가] 항목이 휴지통에 있는지 나타내는 플래그
        var isDeleted: Boolean = false
    ) : FolderItem()
}