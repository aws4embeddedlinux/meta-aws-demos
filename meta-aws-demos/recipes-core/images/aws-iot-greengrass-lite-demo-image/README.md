# aws-iot-greengrass-lite-demo-image
AWS IoT Greengrass is software that extends cloud capabilities to local
devices. This enables devices to collect and analyze data closer to the
source of information, react autonomously to local events, and communicate
securely with each other on local networks. Local devices can also
communicate securely with AWS IoT Core and export IoT data to the AWS Cloud.
AWS IoT Greengrass developers can use AWS Lambda functions and prebuilt
connectors to create serverless applications that are deployed to devices
for local execution.

AWS Greengrass Lite is the AWS IoT Greengrass runtime for constrained devices.

The Greengrass Lite nucleus provides a smaller alternative to the
Classic nucleus for Greengrass v2 deployments.

Greengrass Lite aims to maintain compatibility with the Classic nucleus,
and implements a subset of its functionality.

For other installation possibilities look here:
https://github.com/aws-greengrass/aws-greengrass-lite

## BUILDING

Init build environment

```bash
. init-build-env
```

Configure this image

```bash
export IMAGE=aws-iot-greengrass-lite-demo-image
```

Configure a device e.g. raspberrypi-64

```bash
export DEVICE=raspberrypi-64
```

Build

```bash
bitbake $IMAGE
```


## INSTALLATION

1. Download rpi imager for your OS (mac, win, linux)
https://www.raspberrypi.com/documentation/computers/getting-started.html#raspberry-pi-imager

2. Select “use custom img” and flash demo image contained in this zip file
on your SD-Card. The RPI imager auto rejects the SD card after it completes flashing the image,
you need to uncheck this option or need to plug in again to transfer the unzipped connection kit.

3. Mount the boot fat partition and copy Connection Kit Zip onto it.

- This needs to be done after you flashed your SD-Card. (Step 2)
- This is done depending on your OS in Explorer, nemo, bash, Finder.
- It is the only fat partition on the sd card

4. a) You can configure the ethernet adapter ip address by editing the cmdline.txt
file on the fat partition to append eg.: ip=192.168.0.69::192.168.0.1:255.255.255.0:rpi:eth0:off

4. b) You can create a wpa_supplicant.conf file in the fat partition to configure your WIFI.
It uses this https://linux.die.net/man/5/wpa_supplicant.conf format.
```conf
network={
    ssid="<YOUR NETWORK NAME>"
    psk="<YOUR NETWORK PASSWORD>"
}
```

4. c) You can create a wlan.network file in the fat partition to do the ip configuration of your wlan,
default is this, DHCP. It uses this https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html
format.

```conf
[Match]
Name=wl*

[Network]
DHCP=ipv4

[DHCP]
RouteMetric=20
ClientIdentifier=mac
```

static ip example:

```conf
[Match]
Name=wl*

[Network]
DHCP=no
Address=192.168.0.123/24
Gateway=192.168.0.1
DNS=8.8.8.8 8.8.4.4
```

5. Unmount, remove sd-card, put in Raspberry Pi and boot.

6. Password for "root" user is empty. Please note that this can be a security risk and should be changed
when using device in a public environment! SSH is enabled by default to connect over the network!
You can use the thing name to connect to the device via ssh, this is set as hostname,
and annouced by mdns. Note that underscores in a thing name will be removed!

```bash
Login: root
Password:
```

7. When logged in you can check the status of the installation by running
systemctl status --with-dependencies greengrass-lite.target

## A/B update example made with [meta-rauc](https://github.com/rauc/meta-rauc-community)

Set IMAGE to aws-iot-greengrass-lite-demo-image.
```
export IMAGE=aws-iot-greengrass-lite-demo-image
```

This image works only with raspberry pi. cause of bootloader settings.
Setting DEVICE to raspberrypi-64
```
export DEVICE=raspberrypi-64
```

First compile, enable local use of openssl
```
bitbake openssl-native -caddto_recipe_sysroot
```

Build the image
```
bitbake $IMAGE
```

Build the update bundle - the update that can be applied to the image.
```
bitbake aws-iot-greengrass-lite-demo-bundle
```

Flash the image onto your device e.g.
Be careful device depends on your setup - may sda is your harddisk and not a sd card!!!
You can also extract this and write it with rpi-imager!
```
bzcat aws-iot-greengrass-lite-demo-image-raspberrypi-armv8.rootfs.wic.bz2 | sudo dcfldd of=/dev/sda
```

Then power-on the board and log in. To see that RAUC is configured correctly and can interact with the bootloader, run:
```
rauc status
```

To install an upgrade bundle manually just exec on the device
```
rauc install <URL>
```

To switch manually the slot
```
rauc status mark-active other
```

The rootfs is read only, for development purpose you can mount it read writeable
```
mount -o remount,rw /
```

# DEMO A/B update Greengrass component

## Overview

This Greengrass component, `com.example.AbUpdate`, is designed to manage A/B system updates using RAUC (Robust Auto-Update Controller) on Linux-based systems.

## Description

The component automates the process of installing system updates and verifying the installation. It uses RAUC to handle the A/B update mechanism, ensuring a reliable and fail-safe update process.

## Features

- Automated installation of RAUC bundle updates
- Verification of successful update installation
- Comparison of installed bundle hash with current running slot

## Usage

The component operates in two main phases:

1. **Bootstrap**: Installs the RAUC bundle update.
2. **Startup**: Verifies the installation by comparing the hash of the installed bundle with the currently running slot.

## Configuration

The update file (`update.raucb`) is stored in an S3 bucket. Ensure the S3 URI in the component recipe is updated to point to your specific update file location.
Do modify the bucket name, version etc.

```yaml
---
RecipeFormatVersion: '2020-01-25'
ComponentName: 'com.example.AbUpdate'
ComponentVersion: '1.0.41'
ComponentDescription: 'Manages A/B system updates using RAUC'
ComponentPublisher: 'Example Corp'
ComponentType: 'aws.greengrass.generic'
Manifests:
  - Platform:
      os: 'linux'
      runtime: "*"
    Lifecycle:
      bootstrap:
        Script: |
          echo Bootstrap
          sudo rauc install {artifacts:path}/update.raucb
        RequiresPrivilege: true
      startup:
        Script: |
          echo Startup
          rauc status
          current_booted_slot_bundle_hash=$(rauc status --detailed --output-format=json-pretty | jq -r '.slots[] | select(.[].state == "booted") | .[].slot_status.bundle.hash')
          bundle_hash=$(rauc info --output-format=json-pretty {artifacts:path}/update.raucb | jq -r '.hash')
          if [ "$current_booted_slot_bundle_hash" == "$bundle_hash" ]; then
              echo "Bundle image hash matches the current running slot"
          else
              echo "Bundle image hash differs from the current running slot"
              exit 1
          fi
    Artifacts:
      - URI: 's3://2024-11-27-us-east-1ab-update/update.raucb'
        Unarchive: 'NONE'
```
