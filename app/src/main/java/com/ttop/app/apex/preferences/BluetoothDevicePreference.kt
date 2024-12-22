package com.ttop.app.apex.preferences

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.app.Dialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceViewHolder
import com.google.android.material.card.MaterialCardView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.PreferenceDialogBluetoothDeviceBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.PreferenceUtil


class BluetoothDevicePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {
    init {
        layoutResource = R.layout.custom_preference
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            context.colorControlNormal(),
            SRC_IN
        )
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val cardview = holder.itemView.findViewById<MaterialCardView>(R.id.listCard)
        cardview?.strokeColor = com.ttop.app.apex.libraries.appthemehelper.ThemeStore.accentColor(context)
    }
}

class BluetoothDevicePreferenceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogBluetoothDeviceBinding.inflate(layoutInflater)

        val spinnerArray = ArrayList<String>()
        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, spinnerArray)

        val bluetoothManager =
            context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val mBluetoothAdapter = bluetoothManager.adapter
        val address = ArrayList<String>()
        val name = ArrayList<String>()
        if (context?.let { ContextCompat.checkSelfPermission(it, BLUETOOTH_CONNECT) }
            == PERMISSION_GRANTED) {
            val pairedDevices = mBluetoothAdapter.bondedDevices
            val sortedBtList = pairedDevices.sortedBy { it.name }
            for (bt in sortedBtList) {
                if (!name.contains(bt.name)) {
                    name.add(bt.name)
                    address.add(bt.address)
                }
            }

            for (i in 0..<name.size) {
                spinnerArray.add(name[i] + "::::" + address[i])
            }
        }


        binding.spinner.apply {
            adapter = spinnerAdapter
            arrowColor = requireActivity().accentColor()
            itemColor = requireActivity().accentColor()
            itemListColor = ColorUtil.getComplimentColor(ContextCompat.getColor(requireContext(), R.color.day_night_reversed))
            selectedItemListColor = requireActivity().accentColor()
            underlineColor = ContextCompat.getColor(requireActivity(), android.R.color.transparent)
            hint = ""
        }

        if (PreferenceUtil.bluetoothMac !="") {
            binding.spinner.setSelection(address.indexOf(PreferenceUtil.bluetoothMac))
        }

        return materialDialog(R.string.pref_title_bluetooth_device)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> updateDevice(binding.spinner.selectedItem.toString()) }
            .setView(binding.root)
            .create()
            .centeredColorButtons()
    }

    private fun updateDevice(device: String) {
        val separate = device.split("::::")

        PreferenceUtil.bluetoothDevice = separate[0]
        PreferenceUtil.bluetoothMac = separate [1]

    }

    companion object {
        fun newInstance(): BluetoothDevicePreferenceDialog {
            return BluetoothDevicePreferenceDialog()
        }
    }
}