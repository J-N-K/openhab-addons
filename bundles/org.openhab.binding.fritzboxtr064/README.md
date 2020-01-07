# TR-064 Binding

This binding brings support for internet gateway devices that support the TR-064 protocol.
It can be used to gather information from the device and/or re-configure it.

Tested devices are:
- AVM Fritzbox 6490 Cable

## Supported Things

Three things are supported:

- `rootDevice`: the internet gateway device itself
- `subDevice`: a sub-device of a `rootDevice` (e.g. a WAN interface) 
- `subDeviceLan`: a special type of sub-device that supports MAC-detection

## Discovery

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when using it._


## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._
