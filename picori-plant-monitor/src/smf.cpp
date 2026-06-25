#include "application.h"

EventDispatcher::EventDispatcher() {}

void Application::run() {
  init();

  while (true) {
    idle();
  }
}
