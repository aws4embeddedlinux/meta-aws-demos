# aws-biga-image
Biga is a automotive demo showcasing aws-greengrass.

You need to setup your certs as described here:
https://dev.to/iotbuilders/fleet-provisioning-for-embedded-linux-devices-with-aws-iot-greengrass-4h8b

add this to build/conf/local.conf
```bash
PACKAGECONFIG:pn-greengrass-bin = "fleetprovisioning"
GGV2_DATA_EP     = "xxx-ats.iot.<your aws region>.amazonaws.com"
GGV2_CRED_EP     = "xxx.iot.<your aws region>.amazonaws.com"
GGV2_REGION      = "<your aws region>"
GGV2_THING_NAME  = "ELThing"
# we got this from the cloudformation
GGV2_TES_RALIAS  = "GGTokenExchangeRoleAlias"
GGV2_THING_GROUP = "EmbeddedLinuxFleet"
```

Tested on following devices:
- [`biga` / NXP Goldbox](/meta-aws-demos/conf/devices/biga/README.md)
