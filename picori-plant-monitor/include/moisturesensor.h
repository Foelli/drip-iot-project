#ifndef MOISTURESENSOR_H
#define MOISTURESENSOR_H

#include "smf.h"

class MoistureSensor : public Component {
public:
  MoistureSensor();

  void init() override;
  void handleEvent(Event event) override;
  int readMoisturePercent();
  int getLastRawValue() const;

private:
  int moisturePin;
  int dryRaw;
  int wetRaw;
  int lastRawValue;
  bool calibrationValid;
};

#endif // MOISTURESENSOR_H
