# DRIP

**Distributed Raspberry Irrigation Platform**

DRIP is an IoT system for plant monitoring and automatic irrigation. The project measures soil moisture, air temperature, and air humidity, stores the values in a backend, displays them in a web dashboard, and can automatically activate a water pump when the soil is too dry.

## Table of Contents

* [Project Idea](#project-idea)
* [Motivation and Areas of Use](#motivation-and-areas-of-use)
* [Requirements Fulfillment](#requirements-fulfillment)
* [System Overview](#system-overview)
* [Hardware Components](#hardware-components)
* [Circuit and Schematic](#circuit-and-schematic)
* [Software](#software)
* [Communication](#communication)
* [External Services](#external-services)

## Project Idea

Many houseplants are either watered too rarely or too frequently. DRIP is intended to make plant care easier and more transparent. A microcontroller regularly measures the condition of the plant and, based on a configurable threshold, determines whether watering is required. The measured values are sent to a backend and made visible through a dashboard.

The project consists of two hardware boards:

* a sensor and actuator board for measurement and irrigation
* a display and feedback board for displaying the current measurements

In addition, there is a backend, a database, and a web frontend. This makes the project more than just a single sensor setup: it is a complete IoT system with a device, network communication, data storage, user interface, and notifications.

## Motivation and Areas of Use

The motivation is a practical everyday situation: plants should be reliably cared for even when you cannot constantly monitor them yourself. The system is particularly useful for:

* Houseplants in apartments or offices
* Herb plants such as basil
* Short periods of absence, such as weekends or vacations
* Learning and demonstration purposes for IoT, sensors, actuators, and web communication

The practical benefit is that the plant is not only monitored, but the system can also respond automatically. At the same time, measurements and watering events remain traceable.

## Requirements Fulfillment

| No. | Requirement                                                                       | Implementation in DRIP                                                                                                                                             |
| --- | --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| I   | Use of 2 boards                                                                   | Board 1 reads the sensors and controls the pump. Board 2 retrieves the latest measurements from the backend and displays them using an LCD, LEDs, and a buzzer.    |
| II  | At least 1 sensor/actuator that is not part of the IoT bundle                     | The DHT22 sensor for temperature/air humidity and the water pump are additional components outside a pure standard sensor setup.                                   |
| III | At least 3 sensors/actuators in total, including at least 1 sensor and 1 actuator | The system uses a soil moisture sensor, DHT22, water pump, LCD, LEDs, and buzzer. This provides multiple sensors and actuators.                                    |
| IV  | Smartphone is used                                                                | Watering events can arrive on a smartphone as push/chat notifications via a Discord webhook. The dashboard is currently not implemented as a smartphone interface. |
| V   | Wireless WiFi connection to a computer or cloud                                   | Both boards use WiFi. The sensor board sends measurements to the backend via HTTP. The display board retrieves measurements from the backend via HTTP.             |

## System Overview

```text
+-----------------------------+
| Sensor/Pump Board           |
| - Soil moisture sensor      |
| - DHT22                     |
| - Water pump                |
+--------------+--------------+
               |
               | WiFi + HTTP/JSON
               v
+-----------------------------+
| Ktor Backend                |
| - REST API                  |
| - Plants                    |
| - Measurements              |
| - Watering events           |
+--------------+--------------+
               |
               | Database access
               v
+-----------------------------+
| PostgreSQL                  |
+-----------------------------+
               ^
               |
               | HTTP/JSON
+--------------+--------------+
| Vue Dashboard               |
| - Overview                  |
| - Measurements              |
| - Settings                  |
| - Notifications             |
+--------------+--------------+
               ^
               |
               | WiFi + HTTP/JSON
+--------------+--------------+
|
```
