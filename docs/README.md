# DRIP

**Distributed Raspberry Irrigation Platform**

A self-hosted plant monitoring and watering system using a Raspberry Pi, Raspberry Pi Pico, and a Ktor backend.

## Table of Contents

- [About DRIP](#about-drip)
- [System Overview](#system-overview)
- [Features](#features)
- [Hardware Components](#hardware-components)
- [Software Components](#software-components)
- [Architecture](#architecture)
- [Raspberry Pi Setup](#raspberry-pi-setup)
- [Raspberry Pi Pico Setup](#raspberry-pi-pico-setup)
- [Ktor Backend](#ktor-backend)
- [API Reference](#api-reference)
- [Development](#development)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)

## About DRIP

<details>
<summary>Show details</summary>

DRIP is a self-hosted plant monitoring and watering system designed to keep track of soil moisture levels and automate watering when needed.

The project combines embedded hardware, local networking, backend services, and sensor data collection into one slightly over-engineered plant care system.

</details>

## System Overview

<details>
<summary>Show details</summary>

DRIP uses a Raspberry Pi Pico for hardware-level sensor and actuator control, while the backend is implemented with Ktor and designed to run in any Docker-compatible environment.

The system is intended to remain fully self-hosted and locally controllable.

</details>

## Features

<details>
<summary>Show details</summary>

- Monitor soil moisture levels
- Control watering through water pump
- Collect sensor data from a Raspberry Pi Pico/Arduino Nano RP2040
- Communicate with a Ktor backend
- Support future dashboards, alerts, and automation rules

</details>

## Hardware Components

<details>
<summary>Show details</summary>

[Add list here in the future]

</details>

## Software Components

<details>
<summary>Show details</summary>

[Add list here in the future]

</details>

## Architecture

<details>
<summary>Show details</summary>

```text
+-----------------------+
|     Soil Sensor       |
|   Temperature Sensor  |
+---------+-------------+
          |
          v
+-------------------+
| Raspberry Pi Pico |
| Sensor + Control  |
+---------+---------+
          |
          v
+-------------------+
|   Ktor Backend    |
+---------+---------+
          |
          v
+-------------------+
| Dashboard / API   |
+-------------------+
```

</details>

## Raspberry Pi Setup

<details>
<summary>Show details</summary>

Install dependencies on the Raspberry Pi.

```sh
sudo apt update
sudo apt upgrade
```

Clone the repository.

```sh
git clone git@github.com:Foelli/drip-iot-project.git
cd drip-iot-project
```

Run the backend.

```sh
./gradlew run
```

</details>

## Raspberry Pi Pico Setup

<details>
<summary>Show details</summary>

Flash the Pico firmware using your preferred workflow.

The Pico is responsible for:

- Reading soil moisture sensor data
- Controlling the pump
- Sending data to the Raspberry Pi
- Receiving watering commands

</details>

## Ktor Backend

<details>
<summary>Show details</summary>

The Ktor backend handles communication between the Raspberry Pi, the Pico, and optional clients such as a dashboard or CLI.

Responsibilities include:

- Receiving sensor data
- Exposing API endpoints
- Managing watering logic
- Storing configuration
- Providing status information

</details>

## API Reference

<details>
<summary>Show details</summary>

[Add API endpoints here in the future]

</details>

## Development

<details>
<summary>Show details</summary>

Run the backend locally.

```sh
./gradlew run
```

Run tests.

```sh
./gradlew test
```

Build the project.

```sh
./gradlew build
```

</details>

## Troubleshooting

<details>
<summary>Show details</summary>

[Add troubleshooting notes here in the future]

</details>

## Roadmap

<details>
<summary>Show details</summary>

- Web dashboard
- Historical moisture graphs
- Multiple plant support
- Watering schedules
- Notifications
- Calibration mode
- Docker deployment
- Home Assistant integration

</details>
