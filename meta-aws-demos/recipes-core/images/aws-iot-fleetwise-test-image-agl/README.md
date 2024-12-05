# aws-iot-fleetwise-test-image-agl
AGL version of FleetWise Edge Agent image.

This example showcases how to run, include, configure, and use an AWS IoT Fleetwise Edge Agent.

Together with: https://docs.automotivelinux.org/en/octopus/#01_Getting_Started/02_Building_AGL_Image/08_Building_for_Raspberry_Pi_4/

Apart from adding it to an image along with can-utils
```
IMAGE_INSTALL:append = " aws-iot-fleetwise-edge \
                         can-utils "
```
It's important to configure some of the parameters in order for the FWE to connect to a specific AWS IoT account and to use the right vehicle ID as well as consume data from the appropriate CAN bus.

For more info on what all this means, take a look at the official documentation
https://docs.aws.amazon.com/iot-fleetwise/latest/developerguide/vehicles.html

Example config:
```
CERTIFICATE:pn-aws-iot-fleetwise-edge="-----BEGIN-----\nXXXXX\n-----END-----\n"
PRIVATE_KEY:pn-aws-iot-fleetwise-edge="-----BEGIN-----\nXXXXX\n-----END-----\n"
VEHICLE_NAME:pn-aws-iot-fleetwise-edge="v1"
ENDPOINT_URL:pn-aws-iot-fleetwise-edge="xxx.iot.region.amazonaws.com"
CAN_BUS:pn-aws-iot-fleetwise-edge="vcan0"
```
This will use the `configure-fwe` from [here](https://github.com/aws/aws-iot-fleetwise-edge/blob/main/tools/configure-fwe.sh) to create an appropriate config.json.

More information about AWS IoT FleetWise Edge Agent can be found [here](https://github.com/aws/aws-iot-fleetwise-edge/blob/main/README.md).

## build an qemux86-64 image with FleetWise Edge Agent installed

* Build the image

```bash
bitbake aws-iot-fleetwise-test-image-agl
```
* Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```
