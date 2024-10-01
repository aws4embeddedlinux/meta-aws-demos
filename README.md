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

- [`agl-renesas` / AGL + Renesas](meta-aws-demos/conf/devices/agl-renesas/README.md)
- [`agl-rpi` / AGL + Raspberry Pi Foundation](meta-aws-demos/conf/devices/agl-rpi/README.md)
- [`dart-mx8m` / variscite](meta-aws-demos/conf/devices/dart-mx8m/README.md)
- [`ec2-arm64` / AWS EC2](meta-aws-demos/conf/devices/ec2-arm64/README.md)
- [`ec2-x86-64` / AWS EC2](meta-aws-demos/conf/devices/ec2-x86-64/README.md)
- [`imx8m` / NXP](meta-aws-demos/conf/devices/imx8m/README.md)
- [`qemu-arm64`](meta-aws-demos/conf/devices/qemu-arm64/README.md)
- [`qemu-x86-64`](meta-aws-demos/conf/devices/qemu-x86-64/README.md)
- [`rpi4-32` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/rpi4-32/README.md)
- [`rpi4-64` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/rpi4-64/README.md)
- [`rpi5` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/rpi5/README.md)
- [`phytec` / phytec](meta-aws-demos/conf/devices/phytec/README.md)
- [`ti-am572x-idk` / Texas Instruments](meta-aws-demos/conf/devices/ti-am572x-idk/README.md)
- [`xilinx-zcu104-zynqmp` / Xilinx](meta-aws-demos/conf/devices/xilinx-zcu104-zynqmp/README.md)

## Images
Generally you can build all images for all "Devices", but some combinations do not work or do not make sense!

- [aws-demo-image](meta-aws-demos/recipes-core/images/aws-demo-image/README.md)
- [aws-greengrass-test-image](meta-aws-demos/recipes-core/images/aws-greengrass-test-image/README.md)
- [aws-iot-device-client-test-image](meta-aws-demos/recipes-core/images/aws-iot-device-client-test-image/README.md)
- [aws-webrtc-demo-image](meta-aws-demos/recipes-core/images/aws-webrtc-demo-image/README.md)

> [!IMPORTANT]
> Be careful some of the images require additional local.conf entries, those config.conf files are located in the respective image.
> They are automatically included if the correct environment variables (IMAGE + DEVICE) are set!

## Quick Start

To try out this project in QEMU (default device is `qemu-arm64`), run the following commands:

```bash
git submodule update --init --recursive
. init-build-env
export IMAGE=aws-demo-image
bitbake $IMAGE
runqemu slirp nographic
```

Please also consider these build host [requirements](https://docs.yoctoproject.org/ref-manual/system-requirements.html#required-packages-for-the-build-host).

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

Finally, the images can be built - details in linked readme for each DEMO. Default device is `qemu-arm64`:

```bash
export DEVICE=[DEVICE]
export IMAGE=[IMAGE]
bitbake $IMAGE
```

To build for a different device, set the `DEVICE` (see [here](#Demonstration-environments)) and `IMAGE` environment variable,
like this:

```bash
export DEVICE=ec2-arm64
export IMAGE=aws-demo-image
bitbake $IMAGE
```

For a list of all possible devices, see `meta-aws-demos/conf/devices`.

The `init-build-env` script adds a helper function called `get_devices` which
will list all devices that can be configured. This can be used to build all devices with:

```bash
for d in $(get_devices); do for i in $(get_images); do DEVICE=$d IMAGE=$i bitbake $i; done; done
```

## Adding new platforms

New platforms can be added by adding a new directory under
`meta-aws-demos/conf/devices`. This directory should contain 2 files:

`layers.conf`: This is the file that will be required in `bblayers.conf` when
the product is selected

`config.conf`: This is the file that will be required in `local.conf` when the
product is selected

## Why choose build configurations this way?

`TEMPLATECONF` is a great mechanism for initially populating a build
configuration, but it has a few shortcomings that arise because it will only
write the files if they don't already exist. Because of this, it's not suitable
to _share_ device configuration because users won't automatically get the new
configuration for a build when they change revisions in the repository. The
solution to this problem is quite simple though: Instead of including the build
configuration directly in the template files, the template files instead
`require` a file that is checked into source control. By doing this, users will
automatically get the correct build configuration when changing revisions, but
can still override anything they want in their `local.conf`

## Updating submodules to latest version of specified branch is easy.
The following will update upstream submodule changes recursively:
```bash
git submodule update --remote --init --recursive
```
