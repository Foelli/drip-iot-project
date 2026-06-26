#ifndef MOISTURESENSOR_H
#define MOISTURESENSOR_H

#include "smf.h"

class MoistureSensor : public Component {
public:
  MoistureSensor();

  void init() override;
  void handleEvent(Event event) override;
  void readMoistureLevel();

private:
  int moisturePin;
  int dryValue;
  int wetValue;
};

#endif // MOISTURESENSOR_H
