# Fleet Provisioning for AWS IoT Greengrass

This directory contains scripts and templates for setting up fleet provisioning for AWS IoT Greengrass devices.

## Overview

Fleet provisioning allows you to securely provision IoT devices at scale without hardcoding device certificates. The process works as follows:

1. A CloudFormation stack is deployed to set up all necessary AWS resources
2. A claim certificate is generated and used for initial device authentication
3. When a device boots for the first time, it uses the claim certificate to:
   - Generate a unique device certificate
   - Register itself as an IoT thing
   - Join the specified thing group
   - Obtain the necessary policies for Greengrass operation

## Files

- `create-fleet-provisioning-stack-and-certs.sh`: Main script to set up fleet provisioning infrastructure
- `fleet-provisioning-cfn.yaml`: CloudFormation template that creates all required AWS resources (used by the script above)

## Usage

1. Run the create-fleet-provisioning-stack-and-certs.sh script to set up the AWS infrastructure:
   ```
   ./create-fleet-provisioning-stack-and-certs.sh
   ```

2. Copy the generated local.conf snippet to your Yocto build's local.conf:
   ```
   cat build/fleetprovisioning/local.conf.sample >> build/conf/local.conf
   ```

3. Build your image with fleet provisioning enabled:
   ```
   bitbake aws-iot-greengrass-lite-demo-image
   ```
(This also works with every other demo image where greengrass lite is installed)

4. When the device boots for the first time, the `ggl.gg_pre-fleetprovisioning.service` will:
   - Generate a unique device ID based on MAC address
   - Update the fleet provisioning configuration with this unique ID

5. When the device boots for the first time, the `ggl.gg_fleetprovisioning.service` will:
   - Use the claim certificates to authenticate with AWS IoT
   - Register the device using the fleet provisioning template
   - Store the new device certificates
   - Configure Greengrass to use the new certificates

## Unique Device ID Generation

The fleet provisioning service automatically generates a unique device ID using the MAC address of the first network interface

## Resources Created

The CloudFormation stack creates the following resources:

- IAM roles for token exchange and fleet provisioning
- IoT policies for claim certificates and device certificates
- IoT role alias for token exchange
- IoT thing group for Greengrass devices
- Fleet provisioning template
- Lambda Function for MAC Address Validation

## Reference

This implementation is based on the approach described in the article, adapted to Greengrass lite:
[Fleet Provisioning for Embedded Linux Devices with AWS IoT Greengrass](https://dev.to/iotbuilders/fleet-provisioning-for-embedded-linux-devices-with-aws-iot-greengrass-4h8b)

More information [here](https://github.com/aws-greengrass/aws-greengrass-lite/blob/main/docs/fleet_provisioning/fleet_provisioning.md)
