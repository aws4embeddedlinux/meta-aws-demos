# Demonstrations for the **[meta-aws](https://github.com/aws/meta-aws)** project

[meta-aws](https://github.com/aws/meta-aws) is a [Yocto
Project](https://www.yoctoproject.org/) Bitbake Metadata Layer. It
accelerates building [Amazon Web Services](https://aws.amazon.com)
(AWS) software you can install to [Embedded
Linux](https://elinux.org/Main_Page). Customers use this to build IoT
solutions on AWS.

In this repository, you will find
[meta-aws](https://github.com/aws/meta-aws) demonstrations.  These
demonstrations are both Poky (Yocto Project reference implementation)
based and real hardware based.  Many times, the hardware will be
representative of real use of hardware listed in the [AWS Device
Catalog](https://devices.amazonaws.com).

The number of demonstrations will increase over time and your
contribution is very welcome!

## Demonstration environments
## Devices

Select your desired target environment.  For more information how this
repository is structured see the next section.

These are listed in alphabetical order for ease of selection and
should in no way infer preference.

- [`agl-nxp-goldbox` / AGL + NXP Goldbox](meta-aws-demos/conf/devices/agl-nxp-goldbox/README.md)
- [`agl-renesas` / AGL + Renesas](meta-aws-demos/conf/devices/agl-renesas/README.md)
- [`agl-rpi` / AGL + Raspberry Pi Foundation](meta-aws-demos/conf/devices/agl-rpi/README.md)
- [`dart-mx8m` / variscite](meta-aws-demos/conf/devices/dart-mx8m/README.md)
- [`ec2-arm64` / AWS EC2](meta-aws-demos/conf/devices/ec2-arm64/README.md)
- [`ec2-x86-64` / AWS EC2](meta-aws-demos/conf/devices/ec2-x86-64/README.md)
- [`imx8m` / NXP](meta-aws-demos/conf/devices/imx8m/README.md)
- [`qemu` / qemux86-64](meta-aws-demos/conf/devices/qemu/README.md)
- [`rpi4-32` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/rpi4-32/README.md)
- [`rpi4-64` / Raspberry Pi Foundation](meta-aws-demos/conf/devices/rpi4-64/README.md)
- [`rpi4-64-fleetprovisoning` / Greengrass Nucelus Fleetprovisoning Demo](meta-aws-demos/conf/devices/rpi4-64-fleetprovisoning/README.md)
- [`phytec` /phytec](meta-aws-demos/conf/devices/phytec/README.md)
- [`ti-am572x-idk` / Texas Instruments](meta-aws-demos/conf/devices/ti-am572x-idk/README.md)
- [`xilinx-zcu104-zynqmp` / Xilinx](meta-aws-demos/conf/devices/xilinx-zcu104-zynqmp/README.md)


## Images
Generally you can build all images for all "Devices", but some combinations do not work or do not make sense!
- [aws-biga-image](meta-aws-demos/recipes-core/images/aws-biga-image/README.md)

## Quick Start

To try out this project in QEMU, run the following commands:

```
git submodule update --init --recursive
. init-build-env
export BUILD_DEVICE=qemu
bitbake core-image-minimal
runqemu slirp nographic
```

Please consider also this build host [requirements](https://docs.yoctoproject.org/ref-manual/system-requirements.html#required-packages-for-the-build-host).

## Building

This repository uses submodules and a simple wrapper script to set the default
`TEMPLATECONF` that allows users to select the device they want to build. The
first step is to clone down the submodules:

```bash
git submodule update --init --recursive
```

*NOTE:* When dealing with submodules, it is highly recommended to enable
diffing of submodule history with:
```bash
git config diff.submodule log
```

Next, initialize the build environment, optionally specifying the build directory:

```bash
. init-build-env [BUILDDIR]
```

Finally, the images can be built - details in linked readme for each BUILD_DEVICE:

```bash
bitbake core-image-minimal
```
To build for a different device, set the `BUILD_DEVICE` (see [here](#Demonstration-environments)) environment variable,
like so:

```bash
export BUILD_DEVICE=ec2-arm64
bitbake core-image-minimal
```

For a list of all possible devices, see `meta-aws-demos/conf/devices`

The `init-build-env` script adds a helper function called `get_devices` which
will list all devices that can be configured. This can be used to build all devices with:

```bash
for d in $(get_devices); do BUILD_DEVICE=$d bitbake core-image-minimal; done
```

## Adding new platforms

New platforms can be added by adding a new directory under
`meta-aws-demos/conf/devices`. This directory should contain 2 files:

`layers.conf`: This is the file that will be required in `bblayers.conf` when
the product is selected

`config.conf`: This is the file that will be required in `local.conf` when the
product is selected

## Why choose build configurations this way?

`TEMPLATECONF` is a great mechanism for initially populating build
configuration, but it has a few short comings that arise because it will only
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