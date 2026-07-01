#include "backendapi.h"
#include "secrets.h"
#include <Arduino.h>
#include <ArduinoHttpClient.h>
#include <WiFiNINA.h>
#include <math.h>

BackendAPI::BackendAPI()
    : serverPort(BACKEND_PORT), serverHost(BACKEND_HOST),
      endpointPath(BACKEND_ENDPOINT) {}

void BackendAPI::init() {
  Serial.println("Backend API initialized");
  Serial.print("Backend host: ");
  Serial.println(serverHost);
  Serial.print("Backend port: ");
  Serial.println(serverPort);
  Serial.print("Endpoint: ");
  Serial.println(endpointPath);
}

void BackendAPI::handleEvent(Event event) {}

static int extractIntValue(String json, const char *key, int fallback) {
  String pattern = String("\"") + key + "\":";
  int start = json.indexOf(pattern);
  if (start < 0) {
    return fallback;
  }

  start += pattern.length();
  int end = start;
  while (end < json.length() && (isDigit(json[end]) || json[end] == '-')) {
    end++;
  }

  if (end == start) {
    return fallback;
  }

  return json.substring(start, end).toInt();
}

static bool extractBoolValue(String json, const char *key, bool fallback) {
  String pattern = String("\"") + key + "\":";
  int start = json.indexOf(pattern);
  if (start < 0) {
    return fallback;
  }

  start += pattern.length();
  if (json.substring(start, start + 4) == "true") {
    return true;
  }
  if (json.substring(start, start + 5) == "false") {
    return false;
  }

  return fallback;
}

void BackendAPI::sendSensorReading(SensorReading reading) {
  int temperature = round(reading.temperatureC);
  int soilMoisture = reading.soilMoisture;
  int airMoisture = round(reading.airHumidity);

  String json = "{";
  json += "\"temperature\":";
  json += temperature;
  json += ",";
  json += "\"soilMoisture\":";
  json += soilMoisture;
  json += ",";
  json += "\"airMoisture\":";
  json += airMoisture;
  json += "}";

  Serial.println("Prepared backend payload:");
  Serial.println(json);

  WiFiClient wifi;
  HttpClient client(wifi, serverHost, serverPort);

  String path =
      String(endpointPath) + "/" + String(BACKEND_PLANT_ID) + "/measurements";

  Serial.print("Sending POST ");
  Serial.println(path);

  client.beginRequest();
  client.post(path);
  client.sendHeader("Content-Type", "application/json");
  client.sendHeader("Content-Length", json.length());
  client.beginBody();
  client.print(json);
  client.endRequest();

  int statusCode = client.responseStatusCode();
  String response = client.responseBody();

  Serial.print("Backend status: ");
  Serial.println(statusCode);
  if (response.length() > 0) {
    Serial.println("Backend response:");
    Serial.println(response);
  }
  Serial.println();

  client.stop();
}

WateringConfig BackendAPI::getWateringConfig() {
  WateringConfig config = {
      WATERING_ENABLED,
      WATERING_MIN_MOISTURE_PERCENT,
      PUMP_RUNTIME_MS,
      WATER_SETTLE_MS,
  };

  WiFiClient wifi;
  HttpClient client(wifi, serverHost, serverPort);

  String path = String(endpointPath) + "/" + String(BACKEND_PLANT_ID) +
                "/watering-config";

  Serial.print("Fetching GET ");
  Serial.println(path);

  client.get(path);

  int statusCode = client.responseStatusCode();
  String response = client.responseBody();

  Serial.print("Watering config status: ");
  Serial.println(statusCode);

  if (statusCode == 200) {
    config.enabled = extractBoolValue(response, "enabled", config.enabled);
    config.moistureThreshold =
        extractIntValue(response, "moistureThreshold", config.moistureThreshold);
    config.pumpDurationMs =
        extractIntValue(response, "pumpDurationMs", config.pumpDurationMs);
    config.waterSettleMs =
        extractIntValue(response, "waterSettleMs", config.waterSettleMs);
  } else {
    Serial.println("Using local watering fallback config");
  }

  client.stop();
  return config;
}

void BackendAPI::sendWateringEvent(int moistureBefore,
                                   unsigned long pumpDurationMs) {
  String json = "{";
  json += "\"moistureBefore\":";
  json += moistureBefore;
  json += ",";
  json += "\"pumpDurationMs\":";
  json += pumpDurationMs;
  json += "}";

  WiFiClient wifi;
  HttpClient client(wifi, serverHost, serverPort);

  String path = String(endpointPath) + "/" + String(BACKEND_PLANT_ID) +
                "/watering-events";

  Serial.print("Sending POST ");
  Serial.println(path);
  Serial.println(json);

  client.beginRequest();
  client.post(path);
  client.sendHeader("Content-Type", "application/json");
  client.sendHeader("Content-Length", json.length());
  client.beginBody();
  client.print(json);
  client.endRequest();

  int statusCode = client.responseStatusCode();
  String response = client.responseBody();

  Serial.print("Watering event status: ");
  Serial.println(statusCode);
  if (response.length() > 0) {
    Serial.println("Watering event response:");
    Serial.println(response);
  }

  client.stop();
}
