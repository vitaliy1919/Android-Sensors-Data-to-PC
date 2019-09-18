package com.example.sensorstest

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.io.InterruptedIOException
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var gyro: Sensor
    lateinit var sensorManager: SensorManager
    lateinit var sensorsScrollView: ScrollView
    private lateinit var accelerometer: Sensor;
    private lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: Socket
    private lateinit var context: MainActivity
    public lateinit var connectionStatus: TextView
    public val sensorCheckBoxes = ArrayList<Pair<CheckBox, Sensor>>()
    lateinit var monitorThread: Thread
    lateinit var monitorThreadRunnable: SensorsServerRunnable
    val queue = ConcurrentLinkedQueue<String>()
    private val sensors = ArrayList<Sensor>()
    fun toast(s:String){
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        p0?.also { event->
            val json = JSONObject()
            var arr = JSONArray(event.values)
            json.put("type", event.sensor.type)
            json.put("timestamp", event.timestamp)
            json.put("values", arr)
            queue.offer(json.toString())

            Log.i("SensorJson",json.toString())
            Log.i("Sensor", event.sensor.name + ' ' + Arrays.toString(event.values));

        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this);
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        connectionStatus = findViewById(R.id.ConnectionStatus)
        sensorsScrollView = findViewById(R.id.sensorsScrollView)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in deviceSensors)
        {
            if (sensor.isWakeUpSensor())
                continue;
            var sensorCheckBox = CheckBox(this);
            sensorCheckBoxes.add(Pair(sensorCheckBox, sensor));
            sensorCheckBox.text = sensor.name;
                checkBoxLayout.addView(sensorCheckBox)
            }

            var button = findViewById<Button>(R.id.monitorButton);
            button.setOnClickListener( {
                if (button.text.contains("Start", true))
                {
                    button.text = "Stop monitoring";
                    sensorsScrollView.visibility = View.INVISIBLE;
                    monitorThreadRunnable = SensorsServerRunnable(this, 65000)
                    monitorThread = Thread(monitorThreadRunnable)
                    monitorThread.start()
            } else
            {
                button.text = "Start monitoring"
                connectionStatus.text = "Choose sensors to monitor:"
                sensorsScrollView.visibility = View.VISIBLE;
                monitorThreadRunnable.stop()
                monitorThread.interrupt();
            }

        })
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        sensors.add(accelerometer);
//        sensors.add(gyro);
        context = this
        Log.i("Main","On create");
//        Thread (SensorsServerRunnable(this, 65000)).start();
    }
}
