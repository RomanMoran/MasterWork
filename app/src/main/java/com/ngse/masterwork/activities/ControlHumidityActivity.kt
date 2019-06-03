package com.ngse.masterwork.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ngse.masterwork.Constants
import com.ngse.masterwork.R
import com.ngse.masterwork.bluetooth_utils.*
import com.ngse.masterwork.data.ObjectToPass
import com.ngse.masterwork.data.Parameters
import com.ngse.masterwork.bluetooth_utils.ConnectThreadAsync
import com.ngse.masterwork.bluetooth_utils.ReadWriteInteraction
import kotlinx.android.synthetic.main.activity_control_humidity.*
import java.util.*


class ControlHumidityActivity : AppCompatActivity() {

    var myBluetoothService: ReadWriteInteraction? = null
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = intent.getStringExtra(EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_control_humidity)
        progress = ProgressDialog.show(this, "Connecting...", "Please wait!!!")  //show a progress dialog

        ConnectThreadAsync(address) { btSocket ->
            if (btSocket == null) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.")
                finish()
            } else {
                msg("Connected.")
                myBluetoothService = ReadWriteInteraction(handler, btSocket).apply { start() }
            }
            progress?.dismiss()
        }.execute()
        circularView.setOnProgressChangedListener { progress ->
            circularView.label = "$progress%"; applyButton.isEnabled = true
        }

        applyButton.setOnClickListener {
            //setHumidity(circularView.progress)
            myBluetoothService?.write(ObjectToPass(2, circularView.progress))
            applyButton.isEnabled = false
        }

        startTrackingButton.setOnClickListener {
            myBluetoothService?.write(ObjectToPass(1, phoneTime =  Constants.DATE_TIME_FORMAT.format(Calendar.getInstance().time)))
            //startTrackingButton.isEnabled = false
        }

        disconnectButton.setOnClickListener { disconnect() }
    }


    private val handler = Handler {
        when (it.what) {
            MESSAGE_READ -> {
                if (it.obj is Parameters) {
                    val parameters = it.obj as Parameters
                    logTextView.text = "Параметры \n" +
                            "Время последнего изменения = ${Constants.DATE_TIME_FORMAT_HOUR_MINUTE_SEC_DOUBLE.format(
                                Calendar.getInstance().time
                            )} \n " +
                            "Температура = ${parameters.temperature} *C\n" +
                            "Содержание влаги = ${parameters.humidity} %\n" +
                            "Заданная влага = ${parameters.presetHumidity} %\n"
                    parameters.getLastSwitchedOn()?.let {
                        lastStartTextView.text = "Последнее включение было  ${Constants.DATE_TIME_FORMAT.format(it)}"
                    }

                }
            }
            MESSAGE_WRITE -> {
            }
            MESSAGE_TOAST -> {
                msg(it.data.getString("toast"))
            }
        }
        true
    }

    override fun onDestroy() {
        super.onDestroy()
        myBluetoothService?.cancel()
    }

    private fun disconnect() {
        myBluetoothService?.cancel()
        finish() //return to the first layout
    }

    private fun setHumidity(humidity: Int?) {
        humidity?.let { myBluetoothService?.write(it) }
    }

    // fast way to call Toast
    private fun msg(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }


}

