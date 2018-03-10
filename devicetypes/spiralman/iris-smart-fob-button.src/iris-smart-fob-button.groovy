metadata {
  definition (name: "Iris Smart Fob Button",
              namespace: "spiralman",
              author: "Mitch Pond, Thomas Stephens") {
    capability "Button"

    attribute "lastPress", "string"

    command "buttonDown"
    command "buttonUp"
  }
}

def buttonDown() {
  log.debug "${device.label} button down"
  sendEvent(name: 'lastPress', value: now())
}

def buttonUp() {
  log.debug "${device.label} button up"
  def currentTime = now()
  def startOfPress = device.latestState('lastPress').date.getTime()
  def timeDiff = currentTime - startOfPress
  log.debug "${device.label} time diff: ${timeDiff}"
  def holdTimeMillis = (parent.settings.holdTime?:3).toInteger() * 1000
  log.debug "${device.label} hold threshold: ${holdTimeMillis}"

  if (timeDiff > 0) {
    if (timeDiff > holdTimeMillis) {
      hold()
    }
    else {
      pushed()
    }
  }
}

private pushed() {
  log.debug "${device.label} pushed"
  sendEvent(name: "button", value: "pushed")
}

private held() {
  log.debug "${device.label} held"
  sendEvent(name: "button", value: "held")
}
