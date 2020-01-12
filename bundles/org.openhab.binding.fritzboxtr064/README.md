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

The root device needs to be added manually, after that sub-devices are detected automatically.
It is highly recommended to configure that way and NOT rely on manual configuration.

## Thing Configuration

All things have a `refresh``parameter.
It sets the refresh-interval in seconds for each device channel.
The default value is 60.

### `rootdevice`

The `host` parameter is needed to communicate with the device.
It can be a hostname or an IP address and defaults to `fritz.box`.

For accessing the device you need to supply credentials.
If you did only configure password authentication for your device, the `user` parameter needs to be set to `dslf-config`.
This is also the default value if the parameter is not set.
The second credential parameter is `password` and is mandatory.
For security reasons it is highly recommended to set both, username and password.

One or more TAM (telephone answering machines) are supported by most devices.
By setting the `tamIndices` parameter you can instruct the binding to add channels for these devices to the thing.
Values start with `0`.
This is an optional parameter and multiple values are allowed.

Most devices allow to configure call deflections.
If the `callDeflectionIndices` parameter is set, channels for the status of the pre-configured call deflections are added.
Values start with `0`, including the number of "Call Blocks" (two configured call-blocks -> first deflection is `2`).
This is an optional parameter and multiple values are allowed.

Most devices support call lists.
The binding can analyze these call lists and provide channels for the number of missed calls, inbound calls, outbound calls and rejected (blocked) calls.
The days for which this analysis takes place can be controlled with the `missedCallDays`, `rejectedCallDays`, `inboundCallDays` and `outboundCallDays`
This is an optional parameter and multiple values are allowed.

### `subdevice`, `subdeviceLan`

Besides the bridge the thing is attached to, sub-devices have a `uuid` parameter.
This is the UUID/UDN of the device and a mandatory parameter.
Since the value can only be determined by examining the SCPD of the root device it is highly recommended to use discovery for adding sub-devices.

For `subdeviceLan` devices (type detected automatically during discovery) a second parameter is available `macOnline`.
It adds a channel for each MAC (format 11:11:11:11:11:11) that shows the online status of the respective device.
This is an optional parameter and multiple values are allowed.

## Channels

| channel | item-type | description |
|----|----|----|
| `callDeflectionEnable` | `Switch`| Enable/Disable the call deflection setup with the given index. |
| `deviceLog` | `String`| A string containing the last log messages. |
| `dslCRCErrors` | `Number:Dimensionless`| DSL CRC Errors |
| `dslDownstreamNoiseMargin` | `Number:Dimensionless`| DSL Downstream Noise Margin |
| `dslDownstreamNoiseMargin` | `Number:Dimensionless`| DSL Downstream Attenuation |
| `dslEnable` | `Switch`| DSL Enable |
| `dslFECErrors` | `Number:Dimensionless`| DSL FEC Errors |
| `dslHECErrors` | `Number:Dimensionless`| DSL HEC Errors |
| `dslStatus` | `Switch`| DSL Status |
| `dslUpstreamNoiseMargin` | `Number:Dimensionless`| DSL Upstream Noise Margin |
| `dslUpstreamNoiseMargin` | `Number:Dimensionless`| DSL Upstream Attenuation |
| `inboundCalls` | `Number`| Number of inbound calls within the given number of days. |
| `macOnline` | `Switch`| Online status of the device with the given MAC |
| `missedCalls` | `Number`| Number of missed calls within the given number of days. |
| `outboundCalls` | `Number`| Number of outbound calls within the given number of days. |
| `reboot` | `Switch`| Reboot |
| `rejectedCalls` | `Number`| Number of rejected calls within the given number of days. |
| `securityPort` | `Number`| The port for connecting via HTTPS to the TR-064 service. |
| `tamEnable` | `Switch`| Enable/Disable the answering machine with the given index. |
| `tamNewMessages` | `Number`| The number of new messages of the given answering machine. |
| `uptime` | `Number:Time`| Uptime |
| `uptime` | `Number:Time`| Uptime |
| `wanAccessType` | `String`| Access Type |
| `wanConnectionStatus` | `String`| Connection Status |
| `wanIpAddress` | `String`| WAN IP Address |
| `wanMaxDownstreamRate` | `Number:DataTransferRate`| Max. Downstream Rate |
| `wanMaxUpstreamRate` | `Number:DataTransferRate`| Max. Upstream Rate |
| `wanPhysicalLinkStatus` | `String`| Link Status |
| `wanTotalBytesReceived` | `Number:DataAmount`| Total Bytes Received |
| `wanTotalBytesSent` | `Number:DataAmount`| Total Bytes Send |
| `wifi24GHzEnable` | `Switch`| Enable/Disable the 2.4 GHz WiFi device. |
| `wifi5GHzEnable` | `Switch`| Enable/Disable the 5.0 GHz WiFi device. |
| `wifiGuestEnable` | `Switch`| Enable/Disable the guest WiFi. |

## Full Example

