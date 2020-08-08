/* *  Iris Smart Fob
 *
 *  Copyright 2015 Mitch Pond
 *  Copyright 2018 Thomas Stephens
 *	Presence code adapted from SmartThings Arrival Sensor HA device type
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Iris Smart Fob", namespace: "spiralman", author: "Mitch Pond, Thomas Stephens") {
		capability "Battery"
		capability "Button"
    capability "Configuration"

		command "test"

		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0007,0020,0B05", outClusters: "0003,0006,0019", model:"3450-L", manufacturer: "CentraLite"
		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0007,0020,0B05", outClusters: "0003,0006,0019", model:"3450-L2", manufacturer: "CentraLite"
	}

    preferences{
    	section {
    		input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
        		defaultValue: 3, displayDuringSetup: false)
        input ("enableHold", "boolean", title: "True to calculate hold events, false to only send pushed events", defaultValue: false, displayDuringSetup: false)
        }
    }

	tiles(scale: 2) {
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["battery"])
		details(["battery"])
	}
}

def installed() {
  for (i in 1..4) {
    log.debug "Adding button ${i}"
    def child = addChildDevice("spiralman",
                   "Iris Smart Fob Button",
                   "${device.deviceNetworkId}-b${i}",
                   null,
                   [
                     isComponent: true,
                    componentName: "b${i}",
                    label: "${device.displayName} Button ${i}",
                    completedSetup: true,
                    componentLabel: "Button ${i}"
      ])
    child.sendEvent(name: "supportedButtonValues", value: ["pushed"].encodeAsJSON(), displayed: false)
    child.sendEvent(name: "numberOfButtons", value: 1, displayed: false)
  }
  sendEvent(name: "supportedButtonValues", value: ["pushed"].encodeAsJSON(), displayed: false)
  sendEvent(name: "numberOfButtons", value: 4, displayed: false)
}

def parse(String description) {
	//log.debug "Parsing '${description}'"
  def descMap = zigbee.parseDescriptionAsMap(description)
  //log.debug descMap

	def results = []
  if (description?.startsWith('catchall:')) {
		results = parseCatchAllMessage(descMap)
  }
  else if (description?.startsWith('read attr -')) {
    results = parseReportAttributeMessage(descMap)
  }

  log.debug "parsed events: ${results}"

	return results;
}

def configure(){
	[
	"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 2 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 3 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 4 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}", "delay 200"
    ] +
    zigbee.configureReporting(0x0001,0x0020,0x20,20,20,0x01)
}

def parseCatchAllMessage(descMap) {
  if (descMap?.clusterId == "0006" && descMap?.command == "00") {
    def buttonNumber = descMap.sourceEndpoint as int
    //button released
    log.debug "button up command"
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: buttonNumber], descriptionText: "Button ${buttonNumber} was pushed", isStateChange: true)
    return createButtonEvent(buttonNumber)
  }
}

def parseReportAttributeMessage(descMap) {
	if (descMap?.cluster == "0001" && descMap?.attrId == "0020") {
    return createBatteryEvent(getBatteryLevel(descMap.value))
  }
}

private createBatteryEvent(percent) {
	log.debug "Battery level at " + percent
	return createEvent([name: "battery", value: percent])
}

private getButton(button) {
  for (child in getChildDevices()) {
    if (child.componentName == "b${button}") {
      return child
    }
  }
  log.warning "Couldn't find button ${button}"
  return null
}

private createButtonEvent(button) {
  log.debug "Invoking buttonUp on child ${button}"
  def event = getButton(button).buttonUp()
  log.debug "Got event from button: ${event}"
  return event
}

private getBatteryLevel(rawValue) {
	def intValue = Integer.parseInt(rawValue,16)
	def min = 2.1
    def max = 3.0
    def vBatt = intValue / 10
    return ((vBatt - min) / (max - min) * 100) as int
}

def test() {
    log.debug "Test"
	zigbee.refreshData("0","4") + zigbee.refreshData("0","5") + zigbee.refreshData("1","0x0020")
}
