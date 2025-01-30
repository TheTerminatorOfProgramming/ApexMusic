package com.ttop.app.apex.adapter.backup

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.ItemListBackupBinding
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.util.ViewUtil
import java.io.File


class BackupAdapter(
    val activity: FragmentActivity,
    var dataSet: MutableList<File>,
    val backupClickedListener: BackupClickedListener
) : RecyclerView.Adapter<BackupAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListBackupBinding.inflate(LayoutInflater.from(activity), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text = dataSet[position].nameWithoutExtension
        holder.binding.listCard.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
        holder.binding.listCard.strokeColor = accentColor(activity)
    }

    override fun getItemCount(): Int = dataSet.size

    @SuppressLint("NotifyDataSetChanged")
    fun swapDataset(dataSet: List<File>) {
        this.dataSet = ArrayList(dataSet)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemListBackupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.menu.setOnClickListener { view ->
                val popupMenu = PopupMenu(activity, view)
                popupMenu.inflate(R.menu.menu_backup)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    return@setOnMenuItemClickListener backupClickedListener.onBackupMenuClicked(
                        dataSet[bindingAdapterPosition],
                        menuItem
                    )
                }
                popupMenu.show()
            }
            itemView.setOnClickListener {
                backupClickedListener.onBackupClicked(dataSet[bindingAdapterPosition])
            }
        }
    }

    interface BackupClickedListener {
        fun onBackupClicked(file: File)

        fun onBackupMenuClicked(file: File, menuItem: MenuItem): Boolean
    }
}