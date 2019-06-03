package com.ngse.masterwork.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ngse.masterwork.R
import kotlinx.android.synthetic.main.activity_device_list.*



class DevicesListActivity : AppCompatActivity(){

    //Bluetooth
    private var myBluetooth: BluetoothAdapter? = null
    private var pairedDevices: Set<BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        myBluetooth = BluetoothAdapter.getDefaultAdapter()

        if (myBluetooth == null) {
            notAvailable()
        } else if (!myBluetooth!!.isEnabled) {
            val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTon, 1)
        }

        btnPaired.setOnClickListener{ pairedDevicesList() }

    }

    private fun pairedDevicesList() {
        pairedDevices = myBluetooth?.bondedDevices
        val list = ArrayList<String>()

        if (pairedDevices.isNotNullOrEmpty()) {
            for (bt in pairedDevices!!) {
                list.add(bt.name + "\n" + bt.address)
                bt.fetchUuidsWithSdp()
            }
        } else {
            Toast.makeText(applicationContext, "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show()
        }

        devicelist.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        devicelist.onItemClickListener = myListClickListener //Method called when the device from the list is clicked

    }

    private val myListClickListener = AdapterView.OnItemClickListener { av, v, arg2, arg3 ->
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)
        //val i = Intent(this@DevicesListActivity, ControlActivity::class.java)
        val i = Intent(this@DevicesListActivity, ControlHumidityActivity::class.java)
        i.putExtra(EXTRA_ADDRESS, address)
        startActivity(i)
    }

    private fun notAvailable(){
        Toast.makeText(applicationContext, "Bluetooth Device Not Available", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun Set<Any>?.isNotNullOrEmpty() = this!=null &&  isNotEmpty()

}