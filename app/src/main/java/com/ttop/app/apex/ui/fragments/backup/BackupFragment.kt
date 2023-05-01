package com.ttop.app.apex.ui.fragments.backup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ttop.app.apex.adapter.backup.BackupAdapter
import com.afollestad.materialdialogs.input.input
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentBackupBinding
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.BackupHelper
import com.ttop.app.apex.helper.sanitize
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.Share
import com.ttop.app.apex.util.getExternalStoragePublicDirectory
import com.ttop.app.appthemehelper.util.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.util.*


class BackupFragment : Fragment(R.layout.fragment_backup), BackupAdapter.BackupClickedListener {

    private val backupViewModel by viewModels<BackupViewModel>()
    private var backupAdapter: BackupAdapter? = null
    private var defaultPath = "/storage/emulated/0/Download/Apex/Backups"
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBackupBinding.bind(view)
        initAdapter()
        setupRecyclerview()
        backupViewModel.backupsLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                backupAdapter?.swapDataset(it)
            else
                backupAdapter?.swapDataset(listOf())
        }
        backupViewModel.loadBackups()
        val openFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            lifecycleScope.launch(Dispatchers.IO) {
                it?.let {
                    startActivity(Intent(context, RestoreActivity::class.java).apply {
                        data = it
                    })
                }
            }
        }

        binding.createBackup.accentColor()
        binding.restoreBackup.accentColor()
        binding.resetToDefault.accentOutlineColor()
        binding.createBackup.setOnClickListener {
            showCreateBackupDialog()
        }
        binding.restoreBackup.setOnClickListener {
            openFilePicker.launch(arrayOf("application/octet-stream"))
        }

        binding.backupPath.setOnClickListener {
            openFolder()
        }

        binding.backupPath.text = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

        if (PreferenceUtil.isDevModeEnabled){
            binding.resetToDefault.visibility = View.VISIBLE
        }else {
            binding.resetToDefault.visibility = View.GONE
        }

        binding.resetToDefault.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Reset Settings")
            builder.setMessage("Reset Settings to Default?\nThis will reset ALL settings including Shuffle, Bluetooth Autoplay setting and more\nThis App Will Restart to Complete the Reset Process!")

            builder.setPositiveButton(R.string.yes) { _, _ ->
                val id = BuildConfig.APPLICATION_ID
                val path = "/data/data/$id/shared_prefs"
                val file = File(path)
                if (VersionUtils.hasOreo()) {
                    deleteDirectory(file)
                }

                ProcessPhoenix.triggerRebirth(requireActivity())
            }

            builder.setNegativeButton(R.string.no) { _, _ ->
            }
            builder.show().withCenteredButtons()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteDirectory(directory: File) {
       Files.walk(directory.toPath())
       .filter { Files.isRegularFile(it) }
        .map { it.toFile() }
        .forEach {
            if (it.name != "IntroPrefs.xml"){
                it.delete()
            }
        }
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setDataAndType(
            Uri.parse(
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                        + File.separator + "Apex" + File.separator + "Backup" + File.separator
            ), "file/*"
        )
        startActivityForResult(intent, 9999)
    }

    private fun initAdapter() {
        backupAdapter = BackupAdapter(requireActivity(), ArrayList(), this)
        backupAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    private fun checkIsEmpty() {
        val isEmpty = backupAdapter?.itemCount == 0
        binding.backupTitle.isVisible = !isEmpty
        binding.backupRecyclerview.isVisible = !isEmpty
    }

    fun setupRecyclerview() {
        binding.backupRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    @SuppressLint("CheckResult")
    private fun showCreateBackupDialog() {
        materialDialog().show {
            title(res = R.string.action_rename)
            input(prefill = BackupHelper.getTimeStamp()) { _, text ->
                // Text submitted with the action button
                lifecycleScope.launch {
                    BackupHelper.createBackup(requireContext(), text.sanitize())
                    backupViewModel.loadBackups()
                }
            }
            positiveButton(android.R.string.ok)
            negativeButton(R.string.action_cancel)
            setTitle(R.string.title_new_backup)
        }
    }

    override fun onBackupClicked(file: File) {
        lifecycleScope.launch {
            startActivity(Intent(context, RestoreActivity::class.java).apply {
                data = file.toUri()
            })
        }
    }

    @SuppressLint("CheckResult")
    override fun onBackupMenuClicked(file: File, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_delete -> {
                try {
                    file.delete()
                } catch (exception: SecurityException) {
                    showToast(R.string.error_delete_backup)
                }
                backupViewModel.loadBackups()
                return true
            }
            R.id.action_share -> {
                Share.shareFile(requireContext(), file, "*/*")
                return true
            }
            R.id.action_rename -> {
                materialDialog().show {
                    title(res = R.string.action_rename)
                    input(prefill = file.nameWithoutExtension) { _, text ->
                        // Text submitted with the action button
                        val renamedFile =
                            File(file.parent, "$text${BackupHelper.APPEND_EXTENSION}")
                        if (!renamedFile.exists()) {
                            file.renameTo(renamedFile)
                            backupViewModel.loadBackups()
                        } else {
                            showToast(R.string.file_already_exists)
                        }
                    }
                    positiveButton(android.R.string.ok)
                    negativeButton(R.string.action_cancel)
                    setTitle(R.string.action_rename)
                }
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}