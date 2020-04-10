# AWS IoT Greengrass for the Texas Instruments AM574x Industrial Development Kit

In this tutorial, you will build the Embedded Linux image using the Yocto Project delivered as part of the Texas Instruments Processor SDK for Linux.

You may perform the build steps on an EC2 instance, copy the image locally, and then flash the MicroSD locally.  The steps in this tutorial expects you are working on a local workstation.

## Preparation

You will need to complete all preparation steps to complete all the sections in this tutorial successfully.

1. [Download and install the TI Processor SDK for Linux](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview/Download_and_Install_the_SDK.html).
2. Complete section 1.2.2.1 to [install and configure the Linaro GCC toolchain and Yocto Project build prerequisites](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview_Building_the_SDK.html#prerequisites-one-time-setup). We will modify the steps in 1.2.2.2 for adding IoT Greengrass to the distribution.
4. An [AWS Account](https://aws.amazon.com/free) and an [AWS Identity and Access Management (IAM)](https://aws.amazon.com/iam/) with authorization to run the [IoT Greengrass Tutorial](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-gs.html) in the context of your logged in [IAM User](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction_identity-management.html).

## Build and flash the image

The build and flash instructions are very similar to what is in the Texas Instruments documentation for [Processor SDK for Linux Section 1.2.2.2](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview_Building_the_SDK.html#build-steps) with some minor modifications to include IoT Greengrass.

1. Open a terminal window on your workstation.
2. Copy the `local.conf` we will use for this demonstration to your `conf` directory.

   ```bash
   wget https://github.com/aws-samples/meta-aws-demos/am574x_idk/aws_iot_greengrass/am574x_iot_greengrass.conf
   cp am574x_iot_greengrass.conf ~/tisdk/build/conf/am574x_iot_greengrass.conf
   ```
3. Source the Yocto environment script.  This tutorial was written using the latest SDK at the time which was at version 06_02_00_81.

   ```bash
   cd $HOME/tisdk
   ./oe-layertool-setup.sh \
       -f configs/processor-sdk/processor-sdk-06_02_00_81-config.txt
   cd build
   . conf/setenv
   ```

4. The TI Processor SDK includes the meta-aws project in the source tree automatically. To get the latest version of IoT Greengrass, ensure that the repository is up to date and use the `thud` branch.  At the version of the SDK used, the `thud` Yocto Project is supported by the TI SDK.

   ```bash
   cd $HOME/tisdk/sources/meta-aws
   git checkout -t remotes/origin/thud
   git fetch
   ```

5. Ensure the toolchain is configured in your terminal's environment.

   ```bash
   export TOOLCHAIN_PATH_ARMV7=$HOME/gcc-arm-8.3-2019.03-x86_64-arm-linux-gnueabihf
   export TOOLCHAIN_PATH_ARMV8=$HOME/gcc-arm-8.3-2019.03-x86_64-aarch64-linux-gnu
   ```
6. Build the image.

   ```bash
   cd $HOME/tisdk/build
   bitbake -r conf/am574x_iot_greengrass.conf arago-base-tisdk-image
   ```

   After building, the images will be in the following directory.

   ```bash
   $HOME/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm
   ```   
7. Change directory and then create a boot partition archive.

   ```bash
   cd $HOME/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm
   tar -chJf u-boot.tar.xz MLO u-boot.img zImage
   ```

9. Use the TI flashing tools to flash the image.  In this version of the SDK, the flashing tool is in this directory:

   ```bash
   $HOME/ti-processor-sdk-linux-am57xx-evm-06.02.00.81/bin
   ```
10. Because the tool must access peripherals as `root`, we must run with the sudo command.

    ```bash
    sudo ./create-sdcard.sh
    ```

    Follow the instructions at [SD Card Using Custom Images](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview/Processor_SDK_Linux_create_SD_card_script.html#sd-card-using-custom-images).

    Replace UID with your home directory's ID to complete the fully qualified paths.

    You will input the fully qualified path for the boot files:

    ```bash
/home/UID/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm/u-boot.tar.xz
    ```

    And the fully qualified path for the rootfs:

    ```bash
/home/UID/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm/arago-base-tisdk-image-am57xx-evm-20200409163450.rootfs.tar.xz
    ```

11. Unmount the microSD card from your workstation and put into your EVK system.  Boot the system as normal, using a 115200N81 connection speed.

## Go build solutions

Your image with IoT Greengrass is now up and running.  What's next?

When you go through the [IoT Greengrass Tutorial](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-gs.html), know that Module 1 and Module 2 has already been mostly completed.  You will need to follow the [Configure AWS IoT Greengrass on AWS IoT](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-config.html) chapter to initialize your account for IoT Greengrass, create your Greengrass Group, and then apply the `config.json` and credentials to the target device.

## Cleaning up

If you are running locally and do not have intent to continue working on the local workstation, cleanup (with the exception of installed packages) can be done in three simple steps:

```bash
rm -rf $HOME/gcc-arm-8.3-2019.03-x86_64-arm-linux-gnueabihf
rm -rf $HOME/gcc-arm-8.3-2019.03-x86_64-aarch64-linux-gnu
rm -rf $HOME/tisdk
```


© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.
