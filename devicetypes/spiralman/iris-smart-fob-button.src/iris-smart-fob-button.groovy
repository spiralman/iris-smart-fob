metadata {
  definition (name: "Iris Smart Fob Button",
              namespace: "spiralman",
              author: "Mitch Pond, Thomas Stephens") {
    capability "Button"

    command "pushed"
    command "held"
  }
}

def pushed() {
  sendEvent(name: "button", value: "pushed")
}

def held() {
  sendEvent(name: "button", value: "held")
}
