#include "smf.h"
#include "application.h"

EventDispatcher::EventDispatcher() {}

void Application::run() {
  init();

  while (true) {
    idle();
  }
}

Timer::Timer() : m_ticks(0), m_next(0), m_timerID(0), m_dest(0) {}

void Timer::set(uint32_t ticks, Component *dest, uint32_t timerID) {
  m_ticks = ticks;
  m_dest = dest;
  m_timerID = timerID;
  m_next = 0;
}
