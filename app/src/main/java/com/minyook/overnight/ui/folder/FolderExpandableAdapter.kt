// ui.folder/FolderExpandableAdapter.kt
package com.minyook.overnight.ui.folder

import FolderItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minyook.overnight.R // R 파일 경로는 프로젝트에 맞게 수정해주세요.
import java.util.UUID

class FolderExpandableAdapter(
    private val data: MutableList<FolderItem.Group>, // 원본 Group 데이터
    private val onAddClicked: (groupName: String) -> Unit, // '+' 버튼 콜백
    private val onChildClicked: (childName: String) -> Unit // 하위 항목 클릭 콜백
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 뷰 타입 상수
    private val VIEW_TYPE_GROUP = 1
    private val VIEW_TYPE_CHILD = 2

    // 표시될 실제 목록 (그룹 + 펼쳐진 하위 항목)
    private val displayList: MutableList<FolderItem> = calculateDisplayList()

    // -----------------------------------
    // 목록 계산 및 상태 관리
    // -----------------------------------

    private fun calculateDisplayList(): MutableList<FolderItem> {
        val list = mutableListOf<FolderItem>()
        data.forEach { group ->
            list.add(group)
            if (group.isExpanded) {
                list.addAll(group.children)
            }
        }
        return list
    }

    // 외부에서 호출: 새 하위 항목을 그룹에 추가하고 목록을 업데이트
    fun addChildToGroup(groupName: String, childName: String) {
        val targetGroup = data.find { it.name == groupName }

        if (targetGroup != null) {
            val newChild = FolderItem.Child(parentId = targetGroup.id, name = childName)
            targetGroup.children.add(0, newChild)

            // 그룹이 펼쳐져 있다면 화면에 즉시 반영
            if (targetGroup.isExpanded) {
                // 목록을 다시 계산하고 업데이트
                val groupIndex = displayList.indexOf(targetGroup)
                displayList.clear()
                displayList.addAll(calculateDisplayList())
                if (groupIndex != -1) {
                    // 그룹 바로 아래에 삽입되었음을 알림
                    notifyItemInserted(groupIndex + 1)
                } else {
                    notifyDataSetChanged()
                }
            }
        }
    }

    // -----------------------------------
    // RecyclerView 필수 구현
    // -----------------------------------

    override fun getItemViewType(position: Int): Int {
        return when (displayList[position]) {
            is FolderItem.Group -> VIEW_TYPE_GROUP
            is FolderItem.Child -> VIEW_TYPE_CHILD
        }
    }

    override fun getItemCount(): Int = displayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_GROUP) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_group, parent, false)
            GroupViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_child, parent, false)
            ChildViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayList[position]) {
            is FolderItem.Group -> (holder as GroupViewHolder).bind(item)
            is FolderItem.Child -> (holder as ChildViewHolder).bind(item)
        }
    }

    // -----------------------------------
    // Group ViewHolder
    // -----------------------------------

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_group_title)
        private val toggleIcon: ImageView = itemView.findViewById(R.id.iv_expand_toggle)
        private val addButton: ImageButton = itemView.findViewById(R.id.btn_add_child)
        private val groupIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)

        fun bind(group: FolderItem.Group) {
            title.text = group.name

            // '전체 노트' 그룹에만 '+' 버튼 표시
            addButton.visibility = if (group.name == "전체 노트") View.VISIBLE else View.GONE

            // 그룹 아이콘 설정 (예: 휴지통 아이콘)
            if (group.name == "휴지통") {
                groupIcon.setImageResource(R.drawable.ic_delete)
            } else {
                groupIcon.setImageResource(R.drawable.ic_folder)
            }

            // 1. 접기/펴기 아이콘 방향 설정 (위쪽 화살표(ic_arrow_up)가 90도 회전하면 오른쪽을 가리키도록 설정)
            val rotation = if (group.isExpanded) 0f else 270f
            toggleIcon.rotation = rotation

            // 2. 그룹 전체 클릭 리스너 (접기/펴기)
            itemView.setOnClickListener {
                toggleGroupExpansion(group)
            }

            // 3. '+' 버튼 클릭 리스너 (새 항목 추가 다이얼로그 호출)
            addButton.setOnClickListener {
                onAddClicked(group.name)
            }
        }
    }

    // -----------------------------------
    // Child ViewHolder
    // -----------------------------------

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_child_title)

        fun bind(child: FolderItem.Child) {
            title.text = child.name

            // 하위 항목 클릭 시 ChildNotesFragment로 이동
            itemView.setOnClickListener {
                onChildClicked(child.name)
            }
        }
    }

    // -----------------------------------
    // 핵심 로직: 접기/펴기
    // -----------------------------------

    private fun toggleGroupExpansion(group: FolderItem.Group) {
        group.isExpanded = !group.isExpanded

        // 목록 업데이트
        displayList.clear()
        displayList.addAll(calculateDisplayList())

        // 데이터가 변경되었음을 알림
        notifyDataSetChanged()
    }
}