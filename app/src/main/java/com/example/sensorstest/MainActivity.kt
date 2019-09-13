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
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
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


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var gyro: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor;
    private lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: Socket
    private lateinit var context: MainActivity
    private lateinit var connectionStatus: TextView
    private val queue = ConcurrentLinkedQueue<String>()
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
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        p0?.also { event->
            val json = JSONObject()
            var arr = JSONArray(event.values)
            var type = ""
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
                type = "Accelerometer"
            else if (event.sensor.type == Sensor.TYPE_GYROSCOPE)
                type = "Gyroscope"


            json.put("type", type)
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
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in deviceSensors)
        {
            var sensorCheckBox = CheckBox(this);

            sensorCheckBox.text = sensor.name+" " +sensor.resolution+" " +sensor.minDelay;
            checkBoxLayout.addView(sensorCheckBox)
        }
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        context = this
        Log.i("Main","On create");
        Thread {
            context.runOnUiThread(Runnable {
                Toast.makeText(context, "Thread started", Toast.LENGTH_SHORT).show()
                connectionStatus.text = "Waiting for connection"
            })

            serverSocket = ServerSocket(65000);
    //            serverSocket.soTimeout = 10000;
            do {
                try {
                    clientSocket = serverSocket.accept();
                    var output = OutputStreamWriter(clientSocket.getOutputStream());
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                    sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
                    output.flush();
                    context.runOnUiThread(Runnable {
                        Toast.makeText(context, "Pc connected", Toast.LENGTH_SHORT).show()
                        Log.i("Server","Pc connected");
                        connectionStatus.text = "Pc connected"


                    })
                    do {
                        if (!queue.isEmpty()) {
                            val top = queue.poll()
                            output.write(0)
                            output.write(top)
                            output.write(1)
                            Log.i("Sent", top)
                            output.flush()
                        }
                    } while (!Thread.interrupted())
                } catch (e: Exception) {
                    when (e) {
                        is InterruptedIOException -> {
                            context.runOnUiThread(Runnable {
                                Toast.makeText(context, "timeout reached", Toast.LENGTH_SHORT).show()
                                Log.i("Server", "timeout reached")
                            })
                        }
                        is IOException -> {
                            sensorManager.unregisterListener(this)
                            context.runOnUiThread(Runnable {
                                Toast.makeText(context, "timeout reached", Toast.LENGTH_SHORT).show()
                                Log.i("Server", "timeout reached")
                                connectionStatus.text = "Error while sending data\nReconnecting..."

                            })
                        }
                    }
                }
            } while(true);

            }.start();

//        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
//        val deviceList: HashMap<String, UsbDevice> = manager.deviceList
//        deviceList.values.forEach { device ->
//            //your code
//        }
    }
}
