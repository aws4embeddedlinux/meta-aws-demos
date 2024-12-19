# Demonstrations for the **[meta-aws](https://github.com/aws/meta-aws)** project

[meta-aws](https://github.com/aws/meta-aws) is a [Yocto
Project](https://www.yoctoproject.org/) Bitbake Metadata Layer. It
accelerates building [Amazon Web Services](https://aws.amazon.com)
(AWS) software you can install to [Embedded
Linux](https://elinux.org/Main_Page). You can use this to build IoT
solutions on AWS.

In this repository, you will find
[meta-aws](https://github.com/aws/meta-aws) demonstrations.  These
demos are based on both Poky (the Yocto Project reference implementation)
and real hardware.  Many times, the hardware will be
representative of actual uses of hardware listed in the [AWS Device
Catalog](https://devices.amazonaws.com).

The number of demonstrations will increase over time and your
contributions are very welcome!

## Demonstration environments

The DEMOs consist of a combination of a DEVICE, which represent a hardware and an IMAGE showcasing a use-case.

## Device

Select your desired target environment.  These are listed below in
alphabetical order for ease of selection, no preference should be inferred.

- [`aws-ec2-arm64` / AWS EC2](meta-aws-demos/conf/devices/aws-ec2-arm64/README.md)
- [`aws-ec2-x86-64` / AWS EC2](meta-aws-demos/conf/devices/aws-ec2-x86-64/README.md)
- [`imx8m` / NXP](meta-aws-demos/conf/devices/imx8m/README.md)
- [`qemuarm`](meta-aws-demos/conf/devices/qemuarm/README.md)
- [`qemuarm64`](meta-aws-demos/conf/devices/qemuarm64/README.md)
- [`qemux86-64`](meta-aws-demos/conf/devices/qemux86-64/README.md)
- [`raspberrypi-64` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/raspberrypi-64/README.md)
- [`raspberrypi2` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/raspberrypi2/README.md)
- [`rockchip-rv1106`](meta-aws-demos/conf/devices/rockchip-rv1106/README.md)
- [`stm32mp13-disco` / STM](meta-aws-demos/conf/devices/stm32mp13-disco/README.md)
- [`ti-am572x-idk` / Texas Instruments](meta-aws-demos/conf/devices/ti-am572x-idk/README.md)
- [`xilinx-zcu104-zynqmp` / Xilinx](meta-aws-demos/conf/devices/xilinx-zcu104-zynqmp/README.md)

## Images

Generally you can build all images for all "Devices", but some combinations do not work or do not make sense!

- [aws-demo-image](meta-aws-demos/recipes-core/images/aws-demo-image/README.md)
- [aws-iot-device-client-demo-image](meta-aws-demos/recipes-core/images/aws-iot-device-client-demo-image/README.md)
- [aws-iot-fleetwise-test-image-agl](meta-aws-demos/recipes-core/images/aws-iot-fleetwise-test-image-agl/README.md)
- [aws-iot-fleetwise-test-image](meta-aws-demos/recipes-core/images/aws-iot-fleetwise-test-image/README.md)
- [aws-iot-greengrass-demo-image](meta-aws-demos/recipes-core/images/aws-iot-greengrass-demo-image/README.md)
- [aws-iot-greengrass-demo-simple-image](meta-aws-demos/recipes-core/images/aws-iot-greengrass-demo-simple-image/README.md)
- [aws-iot-greengrass-lite-container-demo-image](meta-aws-demos/recipes-core/images/aws-iot-greengrass-lite-container-demo-image/README.md)
- [aws-iot-greengrass-lite-demo-image](meta-aws-demos/recipes-core/images/aws-iot-greengrass-lite-demo-image/README.md)
- [aws-iot-greengrass-lite-demo-simple-image](meta-aws-demos/recipes-core/images/aws-iot-greengrass-lite-demo-simple-image/README.md)
- [aws-iot-greengrass-lite-demo-tiny-image](meta-aws-demos/recipes-core/images/aws-iot-greengrass-lite-demo-tiny-image/README.md)
- [aws-webrtc-demo-image](meta-aws-demos/recipes-core/images/aws-webrtc-demo-image/README.md)

> [!IMPORTANT]
> Be careful some of the images require additional local.conf entries, those config.conf files are located in the respective image.
> They are automatically included if the correct environment variables (IMAGE + DEVICE) are set!

## Quick Start

To try out this project in QEMU (default device is `qemuarm64`), run the following commands:

```bash
git submodule update --init --recursive
. init-build-env
export IMAGE=aws-demo-image
bitbake $IMAGE
runqemu slirp nographic
```

## Build requirements

Please also consider these build host [requirements](https://docs.yoctoproject.org/ref-manual/system-requirements.html#required-packages-for-the-build-host).
-> At least 100GB of free hard disk space is required!

### Ubuntu 24.04

- `sudo sysctl -w kernel.apparmor_restrict_unprivileged_userns=0`  is needed for bitbake

## Setup

This repository uses submodules and a simple wrapper script to set the default
`TEMPLATECONF` that allows users to select the device they want to build. The
first step is to clone down the submodules:

```bash
git submodule update --init --recursive
```

## Building

Next, initialize the build environment, and optionally specify the build directory:

```bash
. init-build-env [BUILDDIR]
```

Finally, the images can be built - details in linked readme for each DEMO. Default device is `qemuarm64`:

```bash
export DEVICE=[DEVICE]
export IMAGE=[IMAGE]
bitbake $IMAGE
```

To build for a different device, set the `DEVICE` (see [here](#Demonstration-environments)) and `IMAGE` environment variable,
like this:

```bash
export DEVICE=aws-c2-arm64
export IMAGE=aws-demo-image
bitbake $IMAGE
```

For a list of all possible devices, see `meta-aws-demos/conf/devices`.

The `init-build-env` script adds a helper function called `get_devices` which
will list all devices that can be configured. This can be used to build all devices with:

```bash
for d in $(get_devices); do for i in $(get_images); do DEVICE=$d IMAGE=$i && echo $DEVICE && echo $IMAGE && bitbake $i; done; done
```

## Adding new images, devices

New platforms can be added by adding a new directory under
`meta-aws-demos/conf/devices`. This directory should contain 2 files:

`layers.conf`: This is the file that will be required in `bblayers.conf` when
the device is selected

`config.conf`: This is the file that will be required in `local.conf` when the
device is selected

New images can be added by adding a new directory under
`meta-aws-demos/recipes-core/images/`. This directory can contain 2 files:

`layers.conf`: This is the file that will be optionally included in `bblayers.conf` when
the image is selected

`config.conf`: This is the file that will be optionally included in `local.conf` when the
image is selected

## Updating submodules to latest version of specified branch is easy.

The following will update upstream submodule changes recursively:
```bash
git submodule update --remote --init --recursive
```
