# AWS IoT Greengrass for the ADLink LEC-PX30

This tutorial was prepared for customers running Robomaker on their
ADLink LEC-PX30 device.

This tutorial was performed on an Amazon EC2 instance with instance
type c5.18xlarge.

In this tutorial, we use the meta-aws branch **zeus** since the ADLINK
repository instruction specifies **zeus**.

You may perform the build steps on an EC2 instance, copy the image
locally, and then flash the MicroSD locally.  The steps in this
tutorial expects you are working on a local workstation.

## Preparation

You will need to complete all preparation steps to complete all the
sections in this tutorial successfully.

1. Perform all workstation preparation steps as defined in the Yocto
   Mega Manual.
2. An [AWS Account](https://aws.amazon.com/free) and an [AWS Identity
   and Access Management (IAM)](https://aws.amazon.com/iam/) with
   authorization to run the [IoT Greengrass
   Tutorial](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-gs.html)
   in the context of your logged in [IAM
   User](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction_identity-management.html).

## Build and flash the image

1. Begin by following the instruction on ADLINK's github page for [LEC PX30 with IPi SMARC](https://github.com/ADLINK/meta-adlink-rockchip/wiki/01.-Build-Yocto-Image-on-LEC-PX30-with-IPi-SMARC).

   We used commands in `bash` shell, in this order, after updating OS packages:

   a. Define the `BSP_FOLDER` variable which is the working base directory.

      ```bash
BSP_FOLDER=~/lec-px30
mkdir ${BSP_FOLDER} && cd ${BSP_FOLDER}
      ```

b. Create a helper function for performing `git` operations:

      ```bash
function clone() {
    git clone $(eval echo \${${1}_URL}) -b $(eval echo \${${1}_B})
    cd $(eval echo \${${1}_D})
    git checkout $(eval echo \${${1}_CH})
    cd ..
}
      ```

   c. Define the variables we will use for the build environment
      configuration.
 
      ```bash
BRANCH=zeus

POKY_URL=git://git.yoctoproject.org/poky.git
POKY_D=poky
POKY_B=${BRANCH}
POKY_CH=f9ef210967ab34168d4a24930987dc0731baf56f

META_OPENEMBEDDED_URL=git://git.openembedded.org/meta-openembedded.git
META_OPENEMBEDDED_D=meta-openembedded
META_OPENEMBEDDED_B=${BRANCH}
META_OPENEMBEDDED_CH=bb65c27a772723dfe2c15b5e1b27bcc1a1ed884c

META_ROCKCHIP_URL=https://github.com/ADLINK-EPM/meta-rockchip.git
META_ROCKCHIP_D=meta-rockchip
META_ROCKCHIP_B=${BRANCH}
META_ROCKCHIP_CH=8f7727fb24e40ad193373f2ec57a2612799a834b

META_ADLINK_ROCKCHIP_URL=https://github.com/adlink/meta-adlink-rockchip.git
META_ADLINK_ROCKCHIP_D=meta-adlink-rockchip
META_ADLINK_ROCKCHIP_B=${BRANCH}
META_ADLINK_ROCKCHIP_CH=441b5f001a7d1716282e80d8f64309708e51f02a

META_ADLINK_SEMA_URL=https://github.com/adlink/meta-adlink-sema.git
META_ADLINK_SEMA_D=meta-adlink-sema
META_ADLINK_SEMA_B=sema4.0
META_ADLINK_SEMA_CH=2151d926328742ff577afd055f15be0a6397a644

META_BROWSER_URL=https://github.com/OSSystems/meta-browser.git
META_BROWSER_D=meta-browser
META_BROWSER_B=${BRANCH}
META_BROWSER_CH=830ef438e81ba5fc915b1855e69f02b2c286b21a

META_CLANG_URL=git://github.com/kraj/meta-clang
META_CLANG_D=meta-clang
META_CLANG_B=${BRANCH}
META_CLANG_CH=81ba160c95b12b2922f99b60bef25ab37a5e2f0e

META_RUST_URL=git://github.com/meta-rust/meta-rust
META_RUST_D=meta-rust
META_RUST_B=master
META_RUST_CH=a012a1027defe28495f06ed522a7a82bdd59a610

META_AWS_URL=git://github.com/aws/meta-aws
META_AWS_D=meta-aws
META_AWS_B=$BRANCH
META_AWS_CH=b5a1707cd23d462e963958fcfda59b56a08e6710
      ```
   5. Clone repositories

      ```bash
clone POKY
clone META_OPENEMBEDDED
clone META_ROCKCHIP
clone META_ADLINK_ROCKCHIP
clone META_ADLINK_SEMA
clone META_BROWSER
clone META_CLANG
clone META_RUST
clone META_AWS
      ```
   6. Initialize environment.
      ```bash
TEMPLATECONF=$PWD/meta-adlink-rockchip/conf/adlink-conf/
source poky/oe-init-build-env
cd ${BSP_FOLDER}
cp meta-adlink-rockchip/conf/adlink-conf/mirror.conf.sample \
   build/conf/mirror.conf
      ```
   7. Perform baseline build
      ```
bitbake core-image-mb -r build/conf/mirror.conf
      ```

2. After the baseline has been built, we will continue on to configure
   the build system for AWS IoT Greengrass.


   
   ```text
# POKY_BBLAYERS_CONF_VERSION is increased each time build/conf/bblayers.conf
# changes incompatibly
POKY_BBLAYERS_CONF_VERSION = "2"

BBPATH = "${TOPDIR}"
BBFILES ?= ""

BBLAYERS ?= " \
  /src/poky-lec-px30/meta \
  /src/poky-lec-px30/meta-poky \
  /src/poky-lec-px30/meta-rockchip \
  /src/poky-lec-px30/meta-adlink-rockchip \
  /src/poky-lec-px30/meta-openembedded/meta-networking \
  /src/poky-lec-px30/meta-openembedded/meta-python \
  /src/poky-lec-px30/meta-openembedded/meta-oe \
  /src/poky-lec-px30/meta-openembedded/meta-filesystems \
  /src/poky-lec-px30/meta-java \
  /src/poky-lec-px30/meta-virtualization \
  /src/poky-lec-px30//meta-browser \
  /src/poky-lec-px30//meta-clang \
  /src/poky-lec-px30//meta-rust \
  /src/poky-lec-px30/meta-aws \
  "
   ```

7. Build the image.

   ```bash
   TEMPLATECONF=$BASEDIR/$DIST/meta-adlink-rockchip/conf/adlink-conf/
   bitbake -r conf/iot_greengrass_idt_full.conf core-image-mini
   ```

   After building, the images will be in the following directory.

   ```bash
   ls tmp/deploy/images/raspberrypi4/*sdimg
   ```

    Where my image happens to be:

    ```bash
    FILE=tmp/deploy/images/raspberrypi4/core-image-minimal-raspberrypi4-20200709170237.rootfs.rpi-sdimg
    ```

8. Image the target device using `dd`.  You can also use an imaging
   tool you are comfortable with. **BE SUPER CAREFUL**
   
   ```bash
   sudo dd if=$FILE of=`/dev/sda bs=1m
   ```

8. Modify config.txt

   In my case, I use the UART to communicate with the Raspberry Pi.  I
   then remove the remark for the `init_uart_baud` and
   `init_uart_clock` properties.
   

9. Eject the SD Card, insert the SD Card to the Raspberry Pi, connect
the UART and Ethernet, and power up.


## Go build solutions

Your image with IoT Greengrass is now up and running.  What's next?

When you go through the [IoT Greengrass
Tutorial](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-gs.html),
Do not run the Quick Start or Module 1.  You will need to follow the
[Configure AWS IoT Greengrass on AWS
IoT](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-config.html)
chapter to initialize your account for IoT Greengrass, create your
Greengrass Group, and then apply the `config.json` and credentials to
the target device.


## References

https://github.com/ADLINK/meta-adlink-rockchip/tree/zeus


© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.
