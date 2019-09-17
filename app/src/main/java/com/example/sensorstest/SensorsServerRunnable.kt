package com.example.sensorstest

import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InterruptedIOException
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

class SensorsServerRunnable:  Runnable{
    internal var activity: MainActivity
    private var port: Int
    constructor(activity: MainActivity, port: Int) {
        this.activity = activity
        this.port = port
    }

    override fun run() {
        activity.runOnUiThread(Runnable {
            Toast.makeText(activity, "Thread started", Toast.LENGTH_SHORT).show()
            activity.connectionStatus.text = "Waiting for connection"
        })

        var serverSocket = ServerSocket(port)
        //            serverSocket.soTimeout = 10000;
        lateinit var clientSocket: Socket
        do {
            try {
                clientSocket = serverSocket.accept()
                val output = OutputStreamWriter(clientSocket.getOutputStream())

                for (checkBox in activity.sensorCheckBoxes) {
                    if (checkBox.first.isChecked)
                        activity.sensorManager.registerListener(
                            activity,
                            checkBox.second,
                            SensorManager.SENSOR_DELAY_FASTEST
                        )
                }
                //                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                //                    sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
                output.flush()
                activity.runOnUiThread(Runnable {
                    Toast.makeText(activity, "Pc connected", Toast.LENGTH_SHORT).show()
                    Log.i("Server", "Pc connected")
                    activity.connectionStatus.text = "Pc connected"


                })
                do {
                    if (!activity.queue.isEmpty()) {
                        val top = activity.queue.poll()
                        output.write(0)
                        output.write(top)
                        output.write(1)
                        Log.i("Sent", top)
                        output.flush()
                    }
                } while (!Thread.interrupted())
            }catch (e: Exception) {
                when (e) {
                    is InterruptedIOException -> {
                        activity.runOnUiThread(Runnable {
                            Toast.makeText(activity, "timeout reached", Toast.LENGTH_SHORT).show()
                            Log.i("Server", "timeout reached")
                        })
                    }
                    is IOException -> {
                        activity.sensorManager.unregisterListener(activity)
                        activity.runOnUiThread(Runnable {
                            Toast.makeText(activity, "timeout reached", Toast.LENGTH_SHORT).show()
                            Log.i("Server", "timeout reached")
                            activity.connectionStatus.text = "Error while sending data\nReconnecting..."

                        })
                    }
                }
            }
        } while (!Thread.interrupted())
        serverSocket.close()
        clientSocket.close()
    }
}
