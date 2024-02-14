package com.ttop.app.apex.ui.fragments.backup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.input.input
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ttop.app.apex.BACKUP_PATH
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.backup.BackupAdapter
import com.ttop.app.apex.databinding.FragmentBackupBinding
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.BackupHelper
import com.ttop.app.apex.helper.sanitize
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.Share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


class BackupFragment : Fragment(R.layout.fragment_backup), BackupAdapter.BackupClickedListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private val backupViewModel by viewModels<BackupViewModel>()
    private var backupAdapter: BackupAdapter? = null
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data

            val uri: Uri? = data?.data
            val file = uri?.path?.let { File(it) } //create path from uri

            val split = file?.path?.split(":".toRegex())?.dropLastWhile { it.isEmpty() }
                ?.toTypedArray() //split the path.


            val fileType = split?.get(1)
            val finalPath = fileType?.let { getExternalStoragePublicDirectory(it).absolutePath }

            if (finalPath != null) {
                showToast(finalPath)
                if (finalPath.endsWith("Apex" + File.separator + "Backups")) {
                    PreferenceUtil.backupPath = finalPath
                }else {
                    if (finalPath.endsWith("Apex")) {
                        PreferenceUtil.backupPath = finalPath + File.separator + "Backups"
                    }else{
                        PreferenceUtil.backupPath = finalPath + File.separator + "Apex" + File.separator + "Backups"
                    }
                }

            }

            val newDir = PreferenceUtil.backupPath?.let { File(it) }
            if (newDir != null) {
                if (!newDir.exists()){
                    newDir.mkdirs()
                }
            }

            /*var path = PreferenceUtil.backupPath?.length?.minus(8)?.let { PreferenceUtil.backupPath!!.substring(0, it) }
            path = path + File.separator + "Lyrics" + File.separator

            val newLyricsDir = File(path)
            if (!newLyricsDir.exists()){
                newLyricsDir.mkdirs()
            }*/
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

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
        binding.pathLabel.setTextColor(requireContext().accentColor())
        binding.resetToDefault.accentOutlineColor()
        binding.createBackup.setOnClickListener {
            showCreateBackupDialog()
        }
        binding.restoreBackup.setOnClickListener {
            openFilePicker.launch(arrayOf("application/octet-stream"))
        }

        binding.backupPath.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())

            builder.setTitle(getString(R.string.backup_path))
            builder.setMessage(getString(R.string.backup_path_desc))

            builder.setPositiveButton(getString(R.string.backup_select_path)) { _, _ ->
                selectFolder()
            }

            builder.setNegativeButton(
                getString(R.string.backup_open_path)
            ) { _, _ ->
                val location = binding.backupPath.text.toString()
                val intent = Intent(Intent.ACTION_VIEW)
                val mydir = Uri.parse("content://$location")
                intent.setDataAndType(mydir, "*/*")
                startActivity(intent)
            }

            val alert = builder.create()
            alert.show()
            val textViewMessage = alert.findViewById(android.R.id.message) as TextView?


            when (PreferenceUtil.fontSize) {
                "12" -> {
                    textViewMessage!!.textSize = 12f
                }

                "13" -> {
                    textViewMessage!!.textSize = 13f
                }

                "14" -> {
                    textViewMessage!!.textSize = 14f
                }

                "15" -> {
                    textViewMessage!!.textSize = 15f
                }

                "16" -> {
                    textViewMessage!!.textSize = 16f
                }

                "17" -> {
                    textViewMessage!!.textSize = 17f
                }

                "18" -> {
                    textViewMessage!!.textSize = 18f

                }

                "19" -> {
                    textViewMessage!!.textSize = 19f
                }

                "20" -> {
                    textViewMessage!!.textSize = 20f
                }

                "21" -> {
                    textViewMessage!!.textSize = 21f
                }

                "22" -> {
                    textViewMessage!!.textSize = 22f
                }

                "23" -> {
                    textViewMessage!!.textSize = 23f
                }

                "24" -> {
                    textViewMessage!!.textSize = 24f
                }
            }
        }

        binding.backupPath.text = PreferenceUtil.backupPath

        binding.resetToDefault.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.reset_settings)
            builder.setMessage(R.string.reset_settings_msg)

            builder.setPositiveButton(R.string.yes) { _, _ ->
                val id = BuildConfig.APPLICATION_ID
                val path = "/data/data/$id/shared_prefs"
                val file = File(path)
                deleteDirectory(file)

                ProcessPhoenix.triggerRebirth(requireActivity())
            }

            builder.setNegativeButton(R.string.no) { _, _ ->
            }
            val alert = builder.create()
            alert.show()
            alert.withCenteredButtons()

            val textViewMessage = alert.findViewById(android.R.id.message) as TextView?

            when (PreferenceUtil.fontSize) {
                "12" -> {
                    textViewMessage!!.textSize = 12f
                }

                "13" -> {
                    textViewMessage!!.textSize = 13f
                }

                "14" -> {
                    textViewMessage!!.textSize = 14f
                }

                "15" -> {
                    textViewMessage!!.textSize = 15f
                }

                "16" -> {
                    textViewMessage!!.textSize = 16f
                }

                "17" -> {
                    textViewMessage!!.textSize = 17f
                }

                "18" -> {
                    textViewMessage!!.textSize = 18f

                }

                "19" -> {
                    textViewMessage!!.textSize = 19f
                }

                "20" -> {
                    textViewMessage!!.textSize = 20f
                }

                "21" -> {
                    textViewMessage!!.textSize = 21f
                }

                "22" -> {
                    textViewMessage!!.textSize = 22f
                }

                "23" -> {
                    textViewMessage!!.textSize = 23f
                }

                "24" -> {
                    textViewMessage!!.textSize = 24f
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        backupViewModel.loadBackups()
    }

    private fun deleteDirectory(directory: File) {
       Files.walk(directory.toPath())
       .filter { Files.isRegularFile(it) }
        .map { it.toFile() }
        .forEach {
            if (it.name != "IntroPrefs.xml"){
                it.delete()
            }
        }
    }

    private fun selectFolder() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        resultLauncher.launch(i)
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

    private fun setupRecyclerview() {
        binding.backupRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    private fun prefillTitle():String {
        return when (BuildConfig.BUILD_TYPE) {
            "debug" -> {
                SimpleDateFormat(
                    "dd-MMM-yyyy HH:mm:ss",
                    Locale.getDefault()
                ).format(Date()) + " [" + requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).longVersionCode + "] [" + getString(R.string.github_edition_beta).replace(
                    ": ",
                    " "
                ) + "]"
            }

            "preview" -> {
                SimpleDateFormat(
                    "dd-MMM-yyyy HH:mm:ss",
                    Locale.getDefault()
                ).format(Date()) + " [" + requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).longVersionCode + "] [" + getString(R.string.github_edition_preview).replace(
                    ": ",
                    " "
                ) + "]"
            }

            "release" -> {
                SimpleDateFormat(
                    "dd-MMM-yyyy HH:mm:ss",
                    Locale.getDefault()
                ).format(Date()) + " [" + requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    0
                ).longVersionCode + "] [" + getString(R.string.github_edition).replace(
                    ": ",
                    " "
                ) + "]"
            }

            else -> {
                getString(R.string.error_load_failed)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun showCreateBackupDialog() {
        materialDialog().show {
            title(res = R.string.action_rename)
            input(prefill = prefillTitle()) { _, text ->
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
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BACKUP_PATH -> {
                activity?.recreate()
            }
        }
    }
}