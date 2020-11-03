# AWS IoT Greengrass for the Raspberry Pi 4

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

The build was performed under the following configuration.  If your
build breaks when working against the tip of the branch, consider
checking out these commit hashes for the respective layers.

```
meta                 
meta-poky            
meta-yocto-bsp       = "dunfell:40e448301edf142dc00a0ae6190017adac1e57b2"
meta-networking      
meta-python          
meta-oe              
meta-filesystems     = "dunfell:2a5c534d2b9f01e9c0f39701fccd7fc874945b1c"
meta-raspberrypi     = "dunfell:806575fc1d1ea3a6852210b83be9f2999d93b0da"
meta-java            = "dunfell:03537feee539526ec9bb0cf4f55dd4eef6badc71"
meta-virtualization  = "dunfell:ff997b6b3ba800978546098ab3cdaa113b6695e1"
meta-aws             = "dunfell:758f2f34ef1461efba0b8bb4a5756a67fbd4e4ce"
```



1. Open a terminal window on your workstation.

   **NOTE** For the sake of this tutorial, the variable `BASE` refers to the
   build environment parent directory.  For many people, this will be
   `$HOME`.  If you are using another partition as the base directory,
   please set it accordingly.

   ```bash
   export BASEDIR=$HOME
   export DIST=poky-rpi4
   export B=dunfell
   ```

   Clone the Poky base layer to include OpenEmbedded Core, Bitbake,
   and so forth to seed the Yocto build environment.

   ```bash
   git clone -b $B git://git.yoctoproject.org/poky.git $BASEDIR/$DIST
   ```

3. Clone additional dependent repositories.  Note that we are cloning
   only what is required for IoT Greengrass.

   ```bash
   git clone -b $B git://git.openembedded.org/meta-openembedded \
       $BASEDIR/$DIST/meta-openembedded
   git clone -b $B git://git.yoctoproject.org/meta-raspberrypi \
       $BASEDIR/$DIST/meta-raspberrypi
   git clone -b $B git://git.yoctoproject.org/meta-virtualization \
       $BASEDIR/$DIST/meta-virtualization
   git clone -b $B git://git.yoctoproject.org/meta-java \
       $BASEDIR/$DIST/meta-java
   git clone -b $B git://github.com/aws/meta-aws \
       $BASEDIR/$DIST/meta-aws
   ```

4. Source the Yocto environment script.  This seeds the `build/conf`
   directory.

   ```bash
   cd $BASEDIR/$DIST
   . ./oe-init-build-env
   ```

5. Copy the `local.conf` we will use for this demonstration to your
   `conf` directory.

   ```bash
   wget https://raw.githubusercontent.com/aws-samples/meta-aws-demos/master/raspberry_pi4/aws_iot_greengrass/rpi4_iot_greengrass_idt.conf
   mv rpi4_iot_greengrass_idt.conf \
      $BASEDIR/$DIST/build/conf/rpi4_iot_greengrass_idt.conf
   ```

6. Modify `conf/bblayers.conf` to include required layers. After
   editing, your conf file should look like the following. Note that
   some success may be achieved by using `bitbake-layer add-layer` but
   many times after adding the java and virtualization layers this
   utility breaks.  On the author's system, `BASEDIR` is `/src`.
   
   ```text
   # POKY_BBLAYERS_CONF_VERSION is increased each time build/conf/bblayers.conf
   # changes incompatibly
   POKY_BBLAYERS_CONF_VERSION = "2"

   BBPATH = "${TOPDIR}"
   BBFILES ?= ""

   BBLAYERS ?= " \
     /src/poky-rpi4/meta \
     /src/poky-rpi4/meta-poky \
     /src/poky-rpi4/meta-openembedded/meta-networking \
     /src/poky-rpi4/meta-openembedded/meta-python \
     /src/poky-rpi4/meta-openembedded/meta-oe \
     /src/poky-rpi4/meta-openembedded/meta-filesystems \
     /src/poky-rpi4/meta-raspberrypi \
     /src/poky-rpi4/meta-java \
     /src/poky-rpi4/meta-virtualization \
     /src/poky-rpi4/meta-aws \
     "
   ```

7. Build the image.

   ```bash
   bitbake -r conf/rpi4_iot_greengrass_idt.conf core-image-minimal
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
   
   First identify the device using `lsblk` and then set it to the 
   output variable. This will help you confirm that this is really 
   the target device you want to image. **BE SUPER CAREFUL**
   
   ```bash
   lsblk
   #DEVICE=<IDENTIFIED DEVICE, i.e.>
   DEVICE=/dev/sda
   sudo dd if=$FILE of=$DEVICE bs=1m
   ```

9. Modify config.txt

   In my case, I use the UART to communicate with the Raspberry Pi.  I
   then remove the remark for the `init_uart_baud` and
   `init_uart_clock` properties.
   

10. Eject the SD Card, insert the SD Card to the Raspberry Pi, connect
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


© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.
