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
  def event = createEvent([name: "Button", value: "pushed", data: [buttonNumber: 1]])
  log.debug "Created event ${event}"
  return event
}
