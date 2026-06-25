#ifndef SMF_H
#define SMF_H

#include <stdint.h>

struct Event {
  uint32_t eventId;
  uint32_t eventData;
};

class Component {
public:
  virtual void init() = 0;
  virtual void handleEvent(Event event) = 0;
};

class EventDispatcher {
public:
  EventDispatcher();
};

#endif // SMF_H