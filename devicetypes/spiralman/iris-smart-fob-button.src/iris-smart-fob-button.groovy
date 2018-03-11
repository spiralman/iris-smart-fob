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
  def timeDiff = 0
  def enableHold = parent.settings.enableHold

  def startOfPress = device.latestState('lastPress').date.getTime()

  if (enableHold && startOfPress != 0) {
    def holdTimeMillis = (parent.settings.holdTime?:3).toInteger() * 1000
    log.debug "${device.label} hold threshold: ${holdTimeMillis}"

    timeDiff = currentTime - startOfPress
    log.debug "${device.label} time diff: ${timeDiff}"
  }

  if (timeDiff > holdTimeMillis) {
    held()
  }
  else {
    pushed()
  }

  sendEvent(name: 'lastPress', value: 0)
}

private pushed() {
  log.debug "${device.label} pushed"
  sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1])
}

private held() {
  log.debug "${device.label} held"
  sendEvent(name: "button", value: "held", data: [buttonNumber: 1])
}
