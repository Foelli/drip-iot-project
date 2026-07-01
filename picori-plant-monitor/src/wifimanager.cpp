#include "secrets.h"
#include "wifimanager.h"
#include <Arduino.h>
#include <WiFiNINA.h>

WiFiManager::WiFiManager() : ssid(WIFI_SSID), password(WIFI_PASSWORD) {}

void WiFiManager::init() {
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi module not found");
    return;
  }

  Serial.print("WiFi firmware: ");
  Serial.println(WiFi.firmwareVersion());
  Serial.print("Connecting to WiFi: ");
  Serial.println(ssid);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 60) {
    WiFi.begin(ssid, password);
    delay(1000);
    Serial.print(".");
    attempts++;
  }

  Serial.println();

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi connection failed");
    Serial.print("WiFi status: ");
    Serial.println(WiFi.status());
    return;
  }

  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void WiFiManager::handleEvent(Event event) {}

bool WiFiManager::isConnected() { return WiFi.status() == WL_CONNECTED; }
