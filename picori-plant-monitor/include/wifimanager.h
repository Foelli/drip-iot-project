#ifndef WIFIMANAGER_H
#define WIFIMANAGER_H

#include "smf.h"

class WiFiManager : public Component {
public:
  WiFiManager();

  void init() override;
  void handleEvent(Event event) override;
  bool isConnected();

private:
  const char *ssid;
  const char *password;
};

#endif // WIFIMANAGER_H