#ifndef BACKENDAPI_H
#define BACKENDAPI_H

#include "sensorreading.h"
#include "smf.h"

struct WateringConfig {
  bool enabled;
  int moistureThreshold;
  unsigned long pumpDurationMs;
  unsigned long waterSettleMs;
};

class BackendAPI : public Component {
public:
  BackendAPI();

  void init() override;
  void handleEvent(Event event) override;
  void sendSensorReading(SensorReading reading);
  WateringConfig getWateringConfig();
  void sendWateringEvent(int moistureBefore, unsigned long pumpDurationMs);

private:
  int serverPort;
  const char *serverHost;
  const char *endpointPath;
};

#endif // BACKENDAPI_H
