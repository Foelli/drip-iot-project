#include "application.h"
#include "secrets.h"
#include <Arduino.h>

void Firmware::init() {
  Serial.begin(9600);
  statusLED.init();
  wifiManager.init();
  moistureSensor.init();
  airSensor.init();
  waterPump.init();
  backendAPI.init();
  Serial.println("Firmware started");
}

void Firmware::idle() {
  statusLED.toggle();

  SensorReading reading;
  reading.soilMoisture = moistureSensor.readMoisturePercent();
  reading.temperatureC = airSensor.readTemperature();
  reading.airHumidity = airSensor.readHumidity();

  if (reading.soilMoisture < 0) {
    Serial.println("Skipping backend upload: moisture reading is invalid.");
  } else if (wifiManager.isConnected()) {
    backendAPI.sendSensorReading(reading);
  }

  WateringConfig wateringConfig = {
      WATERING_ENABLED,
      WATERING_MIN_MOISTURE_PERCENT,
      PUMP_RUNTIME_MS,
      WATER_SETTLE_MS,
  };

  if (wifiManager.isConnected()) {
    wateringConfig = backendAPI.getWateringConfig();
  }

  if (wateringConfig.enabled && reading.soilMoisture >= 0 &&
      reading.soilMoisture < wateringConfig.moistureThreshold) {
    Serial.println("Soil moisture is below watering threshold.");
    Serial.print("Threshold: ");
    Serial.print(wateringConfig.moistureThreshold);
    Serial.println(" %");
    Serial.print("Pump runtime: ");
    Serial.print(wateringConfig.pumpDurationMs / 1000UL);
    Serial.println(" seconds");

    waterPump.runFor(wateringConfig.pumpDurationMs);

    if (wifiManager.isConnected()) {
      backendAPI.sendWateringEvent(reading.soilMoisture,
                                   wateringConfig.pumpDurationMs);
    }

    Serial.print("Waiting ");
    Serial.print(wateringConfig.waterSettleMs / 1000UL);
    Serial.println(" seconds for water to settle");
    delay(wateringConfig.waterSettleMs);
  }

  Serial.println("Sensor reading");
  Serial.println("--------------");
  Serial.print("Soil raw: ");
  Serial.println(moistureSensor.getLastRawValue());
  Serial.print("Soil: ");
  Serial.print(reading.soilMoisture);
  Serial.println(" %");
  Serial.print("Temp: ");
  Serial.print(reading.temperatureC, 1);
  Serial.println(" C");
  Serial.print("Air:  ");
  Serial.print(reading.airHumidity, 1);
  Serial.println(" %");
  Serial.println();

  Serial.print("Next measurement in ");
  Serial.print(MEASUREMENT_INTERVAL_MS / 60000UL);
  Serial.println(" minutes");
  Serial.println();

  delay(MEASUREMENT_INTERVAL_MS);
}
