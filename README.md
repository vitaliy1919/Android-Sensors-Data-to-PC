# Android Sensors Data to PC
## Overview

This project send the sensor data from your phone to PC using a USB connection and ADB. In order to get the data on your PC you need to use [Android Sensor Data Backend project](https://github.com/vitaliy1919/Android-Sensor-Data-Backend)

## Prerequisites

Android >=5.0
USB Debugging turned on

## Usage

Simply start the app on your phone and after that run Android Sensor Data Backend to get the data on your PC. The label on the screen should change to "PC connected". If that doesn't work make sure that adb forward command on your PC finished successfully, the port on PC is 65000 and that USB debuging is on.

## Important note

Right now only the data from accelerometer and gyroscope is sent. The program can be easily modified to send data from any other sensor on the phone.