#ifndef AIRSENSOR_H
#define AIRSENSOR_H

#include "smf.h"
#include <DHT.h>

class AirSensor : public Component {
public:
  AirSensor();

  void init() override;
  void handleEvent(Event event) override;
  void readAirValues();

private:
  DHT dht;
};

#endif // AIRSENSOR_H
