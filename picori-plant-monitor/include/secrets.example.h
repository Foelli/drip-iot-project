#ifndef SECRETS_H
#define SECRETS_H

#define WIFI_SSID "your-wifi-name"
#define WIFI_PASSWORD "your-wifi-password"

#define BACKEND_HOST "192.168.0.100"
#define BACKEND_PORT 8080
#define BACKEND_ENDPOINT "/api/v1/plants"
#define BACKEND_PLANT_ID 1
#define MEASUREMENT_INTERVAL_MS (30UL * 60UL * 1000UL)

// Measure these with your actual sensor: dry air/dry soil and wet soil/water.
#define MOISTURE_DRY_RAW 1023
#define MOISTURE_WET_RAW 0

#define PUMP_PIN 15
#define WATERING_ENABLED 1
#define WATERING_MIN_MOISTURE_PERCENT 35
#define PUMP_RUNTIME_MS (2UL * 1000UL)
#define WATER_SETTLE_MS (20UL * 1000UL)

#endif // SECRETS_H
