metadata {
  definition (name: "Iris Smart Fob Button",
              namespace: "spiralman",
              author: "Mitch Pond, Thomas Stephens") {
    capability "Button"

    command "buttonUp"

  }
}

def buttonUp() {
  log.debug "${device.label} button up"
  sendEvent(name: "Button", value: "pushed", data: [buttonNumber: 1], descriptionText: "${device.label} was pushed", isStateChange: true)
}
