#ifndef STATUSLED_H
#define STATUSLED_H

#include "smf.h"

class StatusLED : public Component {
public:
  StatusLED();
  ~StatusLED();

  void init();
  void handleEvent(Event event);
  void toggle();

private:
  Timer blinkTimer;

  enum State {
    OFF,
    ON,
  };
  State state;
};

#endif // StatusLED_H
