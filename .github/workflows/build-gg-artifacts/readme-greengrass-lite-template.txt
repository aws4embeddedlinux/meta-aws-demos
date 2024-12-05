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

This zip file contains a demo image that allow you to start quick with
AWS Greengrass. The image is build to work on Raspberry PI 3,4,5 as it is 64bit.

For the latest version of this image look here, also see options to
build, extend it yourself:
https://github.com/aws4embeddedlinux/meta-aws-demos/releases

For other installation possibilities look here:
https://github.com/aws-greengrass/aws-greengrass-lite

This image has been build from this source:
{{ VERSION_LINK }}

The Greengrass Lite package version in this image:  {{ GREENGRASS_PV }}
Build from source code revision: {{ GREENGRASS_SRCREV }}

###############################################################################
#
# INSTALLATION
#
###############################################################################

1. Download rpi imager for your OS (mac, win, linux)
https://www.raspberrypi.com/documentation/computers/getting-started.html#raspberry-pi-imager

2. Select “use custom img” and flash demo image contained in this zip file
on your SD-Card. The RPI imager auto rejects the SD card after it completes flashing the image,
you need to uncheck this option or need to plug in again to transfer the unzipped connection kit.

3. Mount the boot fat partition and copy Connection Kit Zip onto it.
- This needs to be done after you flashed your SD-Card. (Step 2)
- This is done depending on your OS in Explorer, nemo, bash, Finder.
- It is the only fat partition on the sd card

4a) You can configure the ethernet adapter ip address by editing the cmdline.txt
file on the fat partition to append eg.: ip=192.168.0.69::192.168.0.1:255.255.255.0:rpi:eth0:off

4b) You can create a wpa_supplicant.conf file in the fat partition to configure your WIFI.
It uses this https://linux.die.net/man/5/wpa_supplicant.conf format.

network={
    ssid="<YOUR NETWORK NAME>"
    psk="<YOUR NETWORK PASSWORD>"
}

4c) You can create a wlan.network file in the fat partition to do the ip configuration of your wlan,
default is this, DHCP. It uses this https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html
format.

[Match]
Name=wl*

[Network]
DHCP=ipv4

[DHCP]
RouteMetric=20
ClientIdentifier=mac


static ip example:

[Match]
Name=wl*

[Network]
DHCP=no
Address=192.168.0.123/24
Gateway=192.168.0.1
DNS=8.8.8.8 8.8.4.4


5. Unmount, remove sd-card, put in Raspberry Pi and boot.

6. Password for "root" user is empty. Please note that this can be a security risk and should be changed
when using device in a public environment! SSH is enabled by default to connect over the network!
You can use the thing name to connect to the device via ssh, this is set as hostname,
and annouced by mdns. Note that underscores in a thing name will be removed!

Login: root
Password:

7. When logged in you can check the status of the installation by running
systemctl status --with-dependencies greengrass-lite.target


###############################################################################
#
# LICENSE
#
###############################################################################
