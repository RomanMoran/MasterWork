package com.ngse.masterwork.activities

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.ngse.masterwork.R
import com.ngse.masterwork.bluetooth_utils.*
import com.ngse.masterwork.data.SensorType
import com.ngse.masterwork.data.Temperature
import com.ngse.masterwork.data.UltraSonicPass
import kotlinx.android.synthetic.main.activity_control_params.*
import kotlin.random.Random


class ControlParamsActivity : AppCompatActivity() {

    var myBluetoothService: ReadWriteInteraction? = null
    private var progress: ProgressDialog? = null

    var fullPower = false
    var isProcessing = false


    private val PUSHER_APP_KEY = "<INSERT_PUSHER_KEY>"
    private val PUSHER_APP_CLUSTER = "<INSERT_PUSHER_CLUSTER>"
    private val CHANNEL_NAME = "stats"
    private val EVENT_NAME = "new_memory_stat"

    private val TOTAL_MEMORY = 50.0f
    private val LIMIT_MAX_MEMORY = 12.0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = intent.getStringExtra(EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_control_params)
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

        initViews()

        setupChart()
        setupAxes()
        setupData()
        setLegend()
        addRnd.setOnClickListener {
            addEntryDs(Random.nextFloat())
        }
        addRndSecond.setOnClickListener {
            addEntryDht(Random.nextDouble(1.0, 10.0).toFloat())
        }

    }

    private fun setupChart() {
        // disable description text
        chart.description.isEnabled = false
        // enable touch gestures
        chart.setTouchEnabled(true)
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)
        // enable scaling
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        // set an alternative background color
        chart.setBackgroundColor(Color.DKGRAY)
    }

    private fun setupAxes() {
        val xl = chart.xAxis
        xl.textColor = Color.WHITE
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true

        val leftAxis = chart.axisLeft
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMaximum = TOTAL_MEMORY
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(true)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        // Add a limit line
        val ll = LimitLine(LIMIT_MAX_MEMORY, "Upper Limit")
        ll.lineWidth = 2f
        ll.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        ll.textSize = 10f
        ll.textColor = Color.WHITE
        // reset all limit lines to avoid overlapping lines
        leftAxis.removeAllLimitLines()
        leftAxis.addLimitLine(ll)
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true)
    }

    private fun setupData() {
        val set1 = LineDataSet(null, "DS data")
        set1.axisDependency = YAxis.AxisDependency.LEFT
        set1.setColors(ColorTemplate.VORDIPLOM_COLORS[0])
        set1.setCircleColor(Color.WHITE)
        set1.lineWidth = 2f
        set1.circleRadius = 4f
        set1.valueTextColor = Color.WHITE
        set1.valueTextSize = 10f
        // To show values of each point
        set1.setDrawValues(true)

        val set2 = LineDataSet(null, "DHT data")
        set2.axisDependency = YAxis.AxisDependency.LEFT
        set2.setColors(ColorTemplate.VORDIPLOM_COLORS[1])
        set2.setCircleColor(Color.WHITE)
        set2.lineWidth = 2f
        set2.circleRadius = 4f
        set2.valueTextColor = Color.WHITE
        set2.valueTextSize = 10f
        // To show values of each point
        set2.setDrawValues(true)
        val data = LineData(set1, set2)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value)
            }
        })

        // add empty data
        chart.data = data
    }

    private fun setLegend() {
        // get the legend (only possible after setting data)
        val l = chart.legend

        // modify the legend ...
        l.form = Legend.LegendForm.CIRCLE
        l.textColor = Color.WHITE
    }

    private fun createDsSet(): LineDataSet {
        val set = LineDataSet(null, "DS data")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[0])
        set.setCircleColor(Color.WHITE)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 10f
        // To show values of each point
        set.setDrawValues(true)

        return set
    }

    private fun createDhtSet(): LineDataSet {
        val set = LineDataSet(null, "DHT Data")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[1])
        set.setCircleColor(Color.WHITE)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 10f
        // To show values of each point
        set.setDrawValues(true)

        return set
    }

    private fun addEntryDs(temperature: Float) {
        val data = chart.data

        if (data != null) {
            var setDs: ILineDataSet? = data.getDataSetByIndex(0)

            if (setDs == null) {
                setDs = createDsSet()
                data.addDataSet(setDs)
            }

            data.addEntry(Entry(setDs.entryCount.toFloat(), temperature), 0)

            // let the chart know it's data has changed
            data.notifyDataChanged()
            chart.notifyDataSetChanged()

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(15F)

            // move to the latest entry
            chart.moveViewToX(data.entryCount.toFloat())
        }
    }


    private fun addEntryDht(temperature: Float) {
        val data = chart.data

        if (data != null) {
            var setDht: ILineDataSet? = data.getDataSetByIndex(1)

            if (setDht == null) {
                setDht = createDhtSet()
                data.addDataSet(setDht)
            }
            data.addEntry(Entry(setDht.entryCount.toFloat(), temperature), 1)

            // let the chart know it's data has changed
            data.notifyDataChanged()
            chart.notifyDataSetChanged()

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(15F)

            // move to the latest entry
            chart.moveViewToX(data.entryCount.toFloat())
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        progress?.dismiss()
    }

    private fun initViews() {
        switchOn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                myBluetoothService?.write(UltraSonicPass(1, 1))
            }
        }
        switchOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                myBluetoothService?.write(UltraSonicPass(1, 2))
            }
        }
        firstSwitch.setOnCheckedChangeListener { _, isChecked ->
            myBluetoothService?.write(UltraSonicPass(1, 3, 2, isChecked))
        }
        secondSwitch.setOnCheckedChangeListener { _, isChecked ->
            myBluetoothService?.write(UltraSonicPass(1, 3, 5, isChecked))
        }
        thirdSwitch.setOnCheckedChangeListener { _, isChecked ->
            myBluetoothService?.write(UltraSonicPass(1, 3, 4, isChecked))
        }
    }


    /* private fun initViews() {
        switchOn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                fullPower = true
                isProcessing = true
                switchersToggle(true, firstSwitch, secondSwitch, thirdSwitch)
                myBluetoothService?.write(UltraSonicPass(1, 1))
                isProcessing = false
            }
        }
        switchOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                fullPower = false
                isProcessing = true
                switchersToggle(false, firstSwitch, secondSwitch, thirdSwitch)
                myBluetoothService?.write(UltraSonicPass(1, 2))
                isProcessing = false
            }
        }
        firstSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleLogic(isChecked)
            myBluetoothService?.write(UltraSonicPass(1, 3, 2, isChecked))
        }
        secondSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleLogic(isChecked)
            myBluetoothService?.write(UltraSonicPass(1, 3, 2, isChecked))
        }
        thirdSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleLogic(isChecked)
            myBluetoothService?.write(UltraSonicPass(1, 3, 2, isChecked))
        }
    }*/

    private fun toggleLogic(isChecked: Boolean) {
        if (!isChecked) fullPower = false
        if (isChecked && !fullPower) {
            if (isCheckedSwitchers()) {
                switchersToggle(true, switchOn)
            } else {
                switchersToggle(false, switchOff, switchOn)
            }
        } else if (!isChecked && !fullPower) {
            if (isUncheckedSwitchers()) {
                switchersToggle(true, switchOff)
            } else {
                switchersToggle(false, switchOn)
            }
        }
    }

    private fun isCheckedSwitchers(): Boolean = firstSwitch.isChecked && secondSwitch.isChecked && thirdSwitch.isChecked

    private fun isUncheckedSwitchers(): Boolean =
        !firstSwitch.isChecked && !secondSwitch.isChecked && !thirdSwitch.isChecked

    private fun switchersToggle(state: Boolean, vararg views: CompoundButton) {
        views.forEach { it.isChecked = state }
    }


    private val handler = Handler {
        when (it.what) {
            MESSAGE_READ -> {
                if (it.obj is Temperature) {
                    val parameters = it.obj as Temperature
                    when (parameters.sensorType) {
                        SensorType.DHT -> addEntryDht(parameters.temperature)
                        SensorType.DS -> addEntryDs(parameters.temperature)
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

    // fast way to call Toast
    private fun msg(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }
}