// ui.folder/TrashAdapter.kt

package com.minyook.overnight.ui.folder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minyook.overnight.R

// 이 어댑터는 isDeleted=true 인 FolderItem.Child 목록을 받습니다.
class TrashAdapter(
    private val trashedItems: MutableList<FolderItem.Child>, // 삭제를 위해 MutableList로 변경
    private val onOptionsClicked: (folderTitle: String) -> Unit // ⭐ 옵션 클릭 콜백
) : RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {

    override fun getItemCount(): Int = trashedItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashViewHolder {
        // [TODO] 삭제된 항목을 위한 별도의 레이아웃 (예: item_trash_note.xml)이 있다면 그것을 사용하세요.
        // 현재는 item_folder_child.xml을 재활용한다고 가정하고 작성합니다.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_child, parent, false)
        return TrashViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrashViewHolder, position: Int) {
        holder.bind(trashedItems[position])
    }

    // [추가] 아이템 제거 헬퍼 함수
    fun removeItem(folderTitle: String) {
        val index = trashedItems.indexOfFirst { it.name == folderTitle }
        if (index != -1) {
            trashedItems.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class TrashViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // [TODO] ID가 item_folder_child.xml에 맞춰져 있다고 가정합니다.
        private val title: TextView = itemView.findViewById(R.id.tv_child_title)
        private val optionsButton: ImageButton = itemView.findViewById(R.id.btn_child_options) // 옵션 버튼은 숨기거나 복구/삭제 버튼으로 교체 가능

        fun bind(child: FolderItem.Child) {
            title.text = child.name

            optionsButton.setOnClickListener {
                onOptionsClicked(child.name)
            }
            // TODO: 복구 및 영구 삭제 로직 처리를 위한 리스너를 여기에 추가합니다.
        }
    }
}