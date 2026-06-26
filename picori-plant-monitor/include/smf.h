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

class Timer {
  friend class EventDispatcher;

public:
  Timer();
  void set(uint32_t ticks, Component *dest, uint32_t timerID);

private:
  uint32_t m_ticks;
  Timer *m_next;
  uint32_t m_timerID;
  Component *m_dest;
};

#endif // SMF_H