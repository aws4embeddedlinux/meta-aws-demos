# AWS IoT Greengrass for the Texas Instruments AM572x Industrial Development Kit

**DISCLAIMER** This walk-through currently uses the arago
configuration for the `zeus` branch.  This branch will not be used for
the TI Processor SDK.

In this tutorial, you will build the Embedded Linux image using the
Yocto Project delivered as part of the Texas Instruments Processor SDK
for Linux.

You may perform the build steps on an EC2 instance, copy the image
locally, and then flash the MicroSD locally.  The steps in this
tutorial expects you are working on a local workstation.

## Preparation

You will need to complete all preparation steps to complete all the
sections in this tutorial successfully.

1. [Download and install the TI Processor SDK for
   Linux](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview/Download_and_Install_the_SDK.html).
   **Note** we will be using the flash utilities but will not be using
   the Arago source tree from the distribution.
2. Complete section 1.2.2.1 to [install and configure the Linaro GCC
   toolchain and Yocto Project build
   prerequisites](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview_Building_the_SDK.html#prerequisites-one-time-setup). We
   will modify the steps in 1.2.2.2 for adding IoT Greengrass to the
   distribution.
3. An [AWS Account](https://aws.amazon.com/free) and an [AWS Identity
   and Access Management (IAM)](https://aws.amazon.com/iam/) with
   authorization to run the [IoT Greengrass
   Tutorial](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-gs.html)
   in the context of your logged in [IAM
   User](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction_identity-management.html).

## Build and flash the image

The build and flash instructions are very similar to what is in the
Texas Instruments documentation for [Processor SDK for Linux Section
1.2.2.2](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview_Building_the_SDK.html#build-steps)
with some minor modifications to include IoT Greengrass.

1. Open a terminal window on your workstation.  Clone the Arago
   distribution to your workstation.  **NOTE**

   For the sake of this tutorial, the variable `BASE` refers to the
   build environment parent directory.  For many people, this will be
   `$HOME`.  If you are using another partition as the base directory,
   please set it accordingly.

   ```bash
   export BASEDIR=$HOME
   ```

   Clone TI's Arago SDK and use the **zeus** environment.

   ```bash
   cd $BASEDIR
   git clone git://arago-project.org/git/projects/oe-layersetup.git tisdk
   ```

2. Source the Yocto environment script.  This tutorial was written
   using the latest SDK at the time which was at version 06\_02\_00\_81.

   ```bash
   cd $BASEDIR/tisdk
   ./oe-layertool-setup.sh -f configs/arago-zeus-config.txt
   cd build
   . conf/setenv
   ```

3. Copy the `local.conf` we will use for this demonstration to your `conf` directory.

   ```bash
   wget https://raw.githubusercontent.com/aws-samples/meta-aws-demos/master/am572x_idk/aws_iot_greengrass/am572x_iot_greengrass.conf
   mv am572x_iot_greengrass.conf \
      $BASEDIR/tisdk/build/conf/am572x_iot_greengrass.conf
   ```

4. The TI Processor SDK includes the meta-aws project in the source
   tree automatically. In this case, we are using the Arago project so
   we must clone it out specifically.  To get the latest aligned
   version of IoT Greengrass, ensure that the repository is up to date
   and use the `zeus` branch.  At the version of the SDK used, the
   `zeus` Yocto Project is supported by the TI SDK.

   ```bash
   cd $BASEDIR/tisdk/sources
   git clone -b zeus https://github.com/aws/meta-aws
   
   ```
5. Add the `meta-aws` and `meta-java` layers to the build.

   ```text
/src/tisdk/build/conf$ diff bblayers.conf.1 bblayers.conf
30a31,32
> 	/src/tisdk/sources/meta-aws \
   ```
6. Ensure the toolchain is configured in your terminal's environment.

   ```bash
   export TOOLCHAIN_PATH_ARMV7=$BASEDIR/toolchains/gcc-arm-9.2-2019.12-x86_64-arm-none-linux-gnueabihf
   export TOOLCHAIN_PATH_ARMV8=$BASEDIR/toolchains/gcc-arm-9.2-2019.12-x86_64-aarch64-none-linux-gnu
   ```
7. Build the image.

   ```bash
   cd $BASEDIR/tisdk/build
   . conf/setenv
   bitbake -r conf/am572x_iot_greengrass.conf arago-base-tisdk-image
   ```

   After building, the images will be in the following directory.

   ```bash
   $BASEDIR/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm
   ```

8. Change directory and then create a boot partition archive.

   ```bash
   cd $BASEDIR/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm
   tar -chJf u-boot.tar.xz MLO u-boot.img zImage
   ```

9. Use the TI flashing tools to flash the image.  In this version of
   the SDK, the flashing tool is in this directory.

   ```bash
   cd $HOME/ti-processor-sdk-linux-am57xx-evm-06.02.00.81/bin
   ```

10. Because the tool must access peripherals as `root`, we must run with the sudo command.

    ```bash
    sudo ./create-sdcard.sh
    ```

    Follow the instructions at [SD Card Using Custom Images](http://software-dl.ti.com/processor-sdk-linux/esd/docs/latest/linux/Overview/Processor_SDK_Linux_create_SD_card_script.html#sd-card-using-custom-images).

    Replace UID with your home directory's ID to complete the fully
    qualified paths.  You will input the fully qualified path for the
    boot files:

    ```bash
    /home/UID/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm/u-boot.tar.xz
    ```

    And the fully qualified path for the rootfs (your time and date will vary ):

    ```bash
    /home/UID/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm/arago-base-tisdk-image-am57xx-evm-20200409163450.rootfs.tar.xz
    ```
11. Unmount the microSD card from your workstation.

12. Boot the system as normal (turn on with SW3, reset with SW2),
    using a 115200N81 connection speed.  There has been mixed results
    using `screen` and `minicom`.  Best results have been demonstrated
    on `picocom` on Linux and `TeraTerm` on Windows.

    ```bash
    sudo apt-get install picocom
    picocom -b 115200 /dev/ttyUSB1
    ```

## Go build solutions

Your image with IoT Greengrass is now up and running.  What's next?

When you go through the [IoT Greengrass
Tutorial](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-gs.html),
know that Module 1 and Module 2 has already been mostly completed.
You will need to follow the [Configure AWS IoT Greengrass on AWS
IoT](https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-config.html)
chapter to initialize your account for IoT Greengrass, create your
Greengrass Group, and then apply the `config.json` and credentials to
the target device.

## Testing with AWS IoT Device Tester (IDT)

[AWS IoT Device
Tester](https://docs.aws.amazon.com/greengrass/latest/developerguide/device-tester-for-greengrass-ug.html)
requires additional additional software.  To build the image for IDT,
please use the
[am572x\_iot\_greengrass\_idt.conf](am572x_iot_greengrass_idt.conf)
file.  You will need to install package =ntp= for use of =ntpd= or
else Greengrass OTA testing will fail due to potential system time
mismatch causing the SSL handshake during =wget= invocation to fail. 

```bash
wget https://raw.githubusercontent.com/aws-samples/meta-aws-demos/master/am572x_idk/aws_iot_greengrass/am572x_iot_greengrass_idt.conf
mv am572x_iot_greengrass_idt.conf $BASEDIR/tisdk/build/conf/am572x_iot_greengrass_idt.conf
```

If you would also like to perform IDT tests for Stream Manager and
Connectors (which requires Docker), use the following configuration
file:

```bash
wget https://raw.githubusercontent.com/aws-samples/meta-aws-demos/master/am572x_idk/aws_iot_greengrass/am572x_iot_greengrass_idt_full.conf
mv am572x_iot_greengrass_idt.conf $BASEDIR/tisdk/build/conf/am572x_iot_greengrass_idt_full.conf
```

If you are building the **full** version, then you will need to add
OpenJDK to the build. IoT Greengrass version 1.10 and later requires
the Java Development Kit for Stream Manager.  Clone the `meta-java`
repository and align with the `zeus` branch.

```bash
cd $BASEDIR/tisdk/sources/
git clone -b zeus https://git.yoctoproject.org/git/meta-java
```

Add the `meta-java` layer to the build.

```text
/src/tisdk/build/conf$ diff bblayers.conf.1 bblayers.conf
30a31,32
> 	/src/tisdk/sources/meta-aws \
> 	/src/tisdk/sources/meta-java \
```


Download and install IDT to your host system.  In this walkthrough, we are using [IDT v3.0.0 for Linux](https://d232ctwt5kahio.cloudfront.net/greengrass/devicetester_greengrass_linux_3.0.0.zip).

```bash
cd $HOME
unzip ~/Downloads/devicetester_greengrass_linux_3.0.1.zip
```

Configuration files are in directory
`/devicetester_greengrass_linux/configs`.  We have provided sample
config.json and device.json files.  The config.json is setup to use
your CLI authentication (see [Setting Configuration to Run the AWS IoT
Greengrass Qualification
Suite](https://docs.aws.amazon.com/greengrass/latest/developerguide/set-config.html)
in the IoT Greengrass documentation).  The device.json file is
configured for the Device Under Test.

After building, perform the microSD imaging process defined in the
**Build and flash the image** section.

After imaging, insert the microSD to the device. Boot and login to the
system as `root`. Change the password to `@w$4L1f3` using the
`mkpasswd` command.  Identify the IP address of the machine by running
the `ifconfig` command and change device.json accordingly.

After performing the settings, ensure Greengrass is off.  If you had
previously installed certificates and config to the rootfs, then
Greengrass might be started and will interfere with IDT results.

```bash
./devicetester_linux_x86-64 run-suite --suite-id GGQ_1.0.0 --pool-id 1
```

## Cleaning up

If you are running locally and do not have intent to continue working
on the local workstation, cleanup (with the exception of installed
packages) can be done in three simple steps:

```bash
rm -rf $BASEDIR/gcc-arm-8.3-2019.03-x86_64-arm-linux-gnueabihf
rm -rf $BASEDIR/gcc-arm-8.3-2019.03-x86_64-aarch64-linux-gnu
rm -rf $BASEDIR/tisdk
```

# Notes

## OpenJDK compilation problem

If you need to build on =warrior=, set openjdk-8 to be installed for
the stream manager feature, and get a message that says "OS not
supported" then add the following to your local.conf:

```bash
EXTRA_OEMAKE += "DISABLE_HOTSPOT_OS_VERSION_CHECK=ok"
```

## Typical create-sdcard.sh output

```text
$ sudo ./create-sdcard.sh
[sudo] password for me:
Authenticated with cached credentials.


################################################################################

This script will create a bootable SD card from custom or pre-built binaries.

The script must be run with root permissions and from the bin directory of
the SDK

Example:
 $ sudo ./create-sdcard.sh

Formatting can be skipped if the SD card is already formatted and
partitioned properly.

################################################################################


Available Drives to write images to:

#  major   minor    size   name
1:   8        0   31260672 sda

Enter Device Number or n to exit: 1

sda was selected

################################################################################

		**********WARNING**********

	Selected Device is greater then 16GB
	Continuing past this point will erase data from device
	Double check that this is the correct SD Card

################################################################################

Would you like to continue [y/n] : y




/dev/sda is an sdx device
Unmounting the sda drives
 unmounted /dev/sda1
 unmounted /dev/sda2
Current size of sda1 71680 bytes
Current size of sda2 31171584 bytes

################################################################################

	Select 2 partitions if only need boot and rootfs (most users).
	Select 3 partitions if need SDK & other content on SD card.  This is
        usually used by device manufacturers with access to partition tarballs.

	****WARNING**** continuing will erase all data on sda

################################################################################

Number of partitions needed [2/3] : 2


Now partitioning sda with 2 partitions...


################################################################################

		Now making 2 partitions

################################################################################

1024+0 records in
1024+0 records out
1048576 bytes (1.0 MB, 1.0 MiB) copied, 0.119627 s, 8.8 MB/s
DISK SIZE - 32010928128 bytes

################################################################################

		Partitioning Boot

################################################################################
mkfs.fat 4.1 (2017-01-24)
mkfs.fat: warning - lowercase labels might not work properly with DOS or Windows

################################################################################

		Partitioning rootfs

################################################################################
mke2fs 1.44.1 (24-Mar-2018)
/dev/sda2 contains a ext3 file system labelled 'rootfs'
	last mounted on /media/rootfs on Sat Apr 11 19:34:58 2020
Proceed anyway? (y,N) y
Creating filesystem with 7792896 4k blocks and 1949696 inodes
Filesystem UUID: 0a8ef0f2-9008-4d46-a5a0-d3be7b6ae2a2
Superblock backups stored on blocks:
	32768, 98304, 163840, 229376, 294912, 819200, 884736, 1605632, 2654208,
	4096000

Allocating group tables: done                            
Writing inode tables: done                            
Creating journal (32768 blocks): done
Writing superblocks and filesystem accounting information: done   



################################################################################

   Partitioning is now done
   Continue to install filesystem or select 'n' to safe exit

   **Warning** Continuing will erase files any files in the partitions

################################################################################


Would you like to continue? [y/n] : y



Mount the partitions

Emptying partitions


Syncing....

################################################################################

	Choose file path to install from

	1 ) Install pre-built images from SDK
	2 ) Enter in custom boot and rootfs file paths

################################################################################

Choose now [1/2] : 2



################################################################################

  For U-boot and MLO

  If files are located in Tarball write complete path including the file name.
      e.x. $:  /home/user/MyCustomTars/boot.tar.xz

  If files are located in a directory write the directory path
      e.x. $: /ti-sdk/board-support/prebuilt-images/

  NOTE: Not all platforms will have an MLO file and this file can
        be ignored for platforms that do not support an MLO.

  Update: The proper location for the kernel image and device tree
          files have moved from the boot partition to the root filesystem.

################################################################################

Enter path for Boot Partition : /home/me/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm/u-boot.tar.xz

File exists



################################################################################

   For Kernel Image and Device Trees files

    What would you like to do?
     1) Reuse kernel image and device tree files found in the selected rootfs.
     2) Provide a directory that contains the kernel image and device tree files
        to be used.

################################################################################

Choose option 1 or 2 : 1


Reusing kernel and dt files from the rootfs's boot directory



################################################################################

   For Rootfs partition

   If files are located in Tarball write complete path including the file name.
      e.x. $:  /home/user/MyCustomTars/rootfs.tar.xz

  If files are located in a directory write the directory path
      e.x. $: /ti-sdk/targetNFS/

################################################################################

Enter path for Rootfs Partition : /home/me/tisdk/build/arago-tmp-external-arm-toolchain/deploy/images/am57xx-evm/arago-base-tisdk-image-am57xx-evm-20200411233748.rootfs.tar.xz

File exists


################################################################################

	Copying files now... will take minutes

################################################################################

Copying boot partition


Copying rootfs System partition



Syncing...

Un-mount the partitions

Remove created temp directories

Operation Finished


```

© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.
