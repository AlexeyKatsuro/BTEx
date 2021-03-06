package com.e.btex.ui

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.diaryoffilms.ui.common.DataBoundListAdapter
import com.e.btex.databinding.ItemBluettoothDeviceBinding

class DeviceAdapter(private val itemCallBack: (BluetoothDevice)->Unit) : DataBoundListAdapter<BluetoothDevice, ItemBluettoothDeviceBinding>(
        diffCallback = object : DiffUtil.ItemCallback<BluetoothDevice>() {
                override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice) =
                        oldItem.address == newItem.address

                override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice) =
                        oldItem == newItem

        }) {
    override fun createBinding(parent: ViewGroup): ItemBluettoothDeviceBinding {
        return ItemBluettoothDeviceBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
    }

    override fun bind(binding: ItemBluettoothDeviceBinding, item: BluetoothDevice) {
        binding.device = item
        binding.root.setOnClickListener { itemCallBack.invoke(item) }
    }

    fun clear(){
        submitList(mutableListOf())
    }
}