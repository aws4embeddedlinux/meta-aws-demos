https://docs.automotivelinux.org/en/octopus/#01_Getting_Started/02_Building_AGL_Image/08_Building_for_Raspberry_Pi_4/

`bitbake agl-image-boot`

WIP: layer just supports dunfell?

<tr>
<td>agl</td>
<td>h3ulcb</td>
<td><a href="https://www.renesas.com/br/en/solutions/automotive/adas/solution-kits/r-car-starter-kit.html">R-Car H3 Premier</a></td>
<td colspan="4">&nbsp;</td>
</tr>


# AWS IoT Greengrass for the Renesas R-Car H3 and Automotive Grade Linux

You may perform the build steps on an EC2 instance, copy the image
locally, and then flash the MicroSD locally.  The steps in this
tutorial expects you are working on a local workstation.

The majority of the steps were gleaned from [Building for Supported
Renesas
Boards](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/5_3_RCar_Gen_3/)
in the AGL documentation.  We repeat specific steps here to
demonstrate the **exact** steps used and where `meta-aws` was inserted
to the build procedure so you can take it into account when you build
your own.

**NOTE**: We are building with multimedia features that you might not
want to use and this will be called out during the procedure.

## Hardware used

- 1x Renesas R-Car H3 WS2.0, RTP0RC77951SKBX010SA00, S/N 2236
- 1x 5V 8A Adapter, 5.5mm barrel plug
- 1x 8GB SanDisk Ultra microSD
- 1x micro USB cable

The build result will be run from microSD. It will be slower than
running from eMMC, but less involved procedurally.

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

## Prepare, invoke, and flash the build

Due to the size of this build, the build environment was defined in
the AWS Cloud using a `c5.18xlarge` instance type.  Remember that in
the AWS Cloud you pay for what you use, so to be thrifty consider
stopping or terminating your instance after performing the build
tasks.

1. Launch the EC2 instance with the Ubuntu 18.04 AMI.
2. Login to the EC2 instance with `ssh` and prepare system updates.
   
   ```bash
   sudo apt-get update
   sudo apt-get -y upgrade
   ```
3. In the EC2 instance terminal, perform steps on the [Downloading AGL
   Software](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/2_Downloading_AGL_Software/)
       page.
   
   The command line steps we used are here.  Please reference the
   documentation if you would like an explanation of steps.
   
   ```bash
   export AGL_TOP=$HOME/AGL
   echo 'export AGL_TOP=$HOME/AGL' >> $HOME/.bashrc
   mkdir -p $AGL_TOP
   
   mkdir -p $HOME/bin
   export PATH=$HOME/bin:$PATH
   echo 'export PATH=$HOME/bin:$PATH' >> $HOME/.bashrc
   curl https://storage.googleapis.com/git-repo-downloads/repo > $HOME/bin/repo
   chmod a+x $HOME/bin/repo
   ```
   We will be using the Jumping Jellyfish release since it aligns with
   the Yocto Project **dunfell** LTS release.

   ```bash
   cd $AGL_TOP
   mkdir jellyfish
   cd jellyfish
   repo init -b jellyfish -m jellyfish_10.0.0.xml -u https://gerrit.automotivelinux.org/gerrit/AGL/AGL-repo
   repo sync
   ```

5. Now that we have the basics setup, there are some Renesas specific
   parts to organize.  On your local workstation, download the Renesas
   proprietary drivers by following the download instructions in the
   section [Downloading Proprietary
   Drivers](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/5_3_RCar_Gen_3/#1-downloading-proprietary-drivers).
   
   To identify the drivers you will need, invoke this command within
   the EC2 build ennvironment.

   ```bash
   grep -rn ZIP_.= $AGL_TOP/jellyfish/meta-agl/meta-agl-bsp/meta-rcar-gen3/scripts/setup_mm_packages.sh
   ```
   At the time of writing, the command output:
   
   ```text
   3:ZIP_1="R-Car_Gen3_Series_Evaluation_Software_Package_for_Linux-weston8-20191206.zip"
   4:ZIP_2="R-Car_Gen3_Series_Evaluation_Software_Package_of_Linux_Drivers-weston8-20191021.zip"
   ```

   Which are the v3.21.0 payloads.

   Please follow the rest of the steps to download from Renesas to
   your workstation (in this case it is for YP3.1 Dunfell), upload to
   the EC2 instance, and then configure.
   
   We did not try the non-MMP v4.1.0 version.

   From your workstation, upload the files. Replace the 'x' characters
   with real values according to your situation.
   
   ```bash
    scp -i ~/.ssh/xxx.pem ~/Downloads/R-Car*.zip ubuntu@xx.xxx.xxx.xxx:
   ```
   
   This is how we handled the placement of the files:
   
   ```bash
   ssh -i ~/.ssh/xxx.pem ubuntu@xx.xxx.xxx.xxx:
   mkdir XDG_DOWNLOAD_DIR
   mv R*zip XDG_DOWNLOAD_DIR
   export XDG_DOWNLOAD_DIR=$HOME/XDG_DOWNLOAD_DIR
   chmod a+x $XDG_DOWNLOAD_DIR/*.zip
   
   ```

4. In our build, we will be building for the AGL telematics demo in
meta-agl-telematics-demo. To set this up easily for the R-Car,
initialize the build environment with the new `aglsetup.sh` utility.
Its full use is described in the [Initializing Your Build Environment](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/3_Initializing_Your_Build_Environment/)
section of the AGL documentation.

   ```bash
   cd $AGL_TOP/jellyfish
   source meta-agl/scripts/aglsetup.sh \
       -m h3ulcb \
       -b h3ulcb \
       agl-demo agl-devel

   cd $AGL_TOP/jellyfish/bsp/meta-renesas
   sh meta-rcar-gen3/docs/sample/copyscript/copy_evaproprietary_softwares.sh -f ${XDG_DOWNLOAD_DIR}
   ```

5. At this point, the base image is configured but we need to add AWS
   IoT Greengrass.
   
   ```bash
   cd $AGL_TOP/jellyfish
   git clone -b dunfell https://github.com/aws/meta-aws
   bitbake-layers add-layer $AGL_TOP/jellyfish/meta-aws
   ```
6. Edit the passwd and group file in meta-agl-profile-core to overcome
   https://github.com/aws/meta-aws/issues/75 

7. Add the following to local.conf by invoking this command.
   
   ```bash
   echo "IMAGE_INSTALL_append = \"greengrass openssh ntp\"" >> \
    $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   echo "MACHINE_FEATURES_append = \" multimedia\"" >> $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   echo "DISTRO_FEATURES_append = \" use_eva_pkg"\" >> $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   ```
   
8. Invoke the build.

   ```bash
   bitbake agl-demo-platform
   ```

9. Download files.  The dtb and Image files are in /boot in the
   tarball. Because of this, the Image and dtb load from the root file
   system.
   
   The .srec file download and application to QuickFlash or QSPI has
   to be done only once and is here for visibility.  For iterative
   builds, downloading the .tar.bz2 is sufficient.

```bash
cd ~/Downloads
BASEDIR=AGL/jellyfish/h3ulcb/tmp/deploy/images/h3ulcb
FILES="bootparam_sa0.srec
       bl2-h3ulcb.srec
       cert_header_sa6.srec
       bl31-h3ulcb.srec
       tee-h3ulcb.srec
       u-boot-elf-h3ulcb.srec
       agl-demo-platform-h3ulcb.tar.bz2"
for FILE in $FILES; do
    scp -i  ~/.ssh/xxx.pem ubuntu@xx.xxx.xxx.xx:$BASEDIR/$FILE .
done

```

10. Partition the microSD card which only has to be done once per
    microSD card.  Essentially, you will delete all partitions and
    create one large partition.
    
    **WARNING** Make sure you are using the microSD card you intend to
    partition by checking the device first with `lsblk`!

    ```bash
    sudo fdisk /dev/mmcblk0
    => (use 'd' to delete all partitions)
    => (use 'n' to create a new rootfs partition, taking all space)
    => (use 'l' and '83' to label the partition as Linux)
    => w
    ```

11. Create the partition and lay down the rootfs.  Important: do not
    forget to sync filesystems.

    ```bash
    sudo ext3.mkfs /dev/mmcblk0p1
    sudo mount /dev/mmcblk0p1 /mnt
    # note where your mmc is mounted
    sudo tar xfj agl-demo-platform-h3ulcb.tar.bz2 -C /mnt && sync
    ```# AWS IoT Greengrass for the Renesas R-Car H3 and Automotive Grade Linux

You may perform the build steps on an EC2 instance, copy the image
locally, and then flash the MicroSD locally.  The steps in this
tutorial expects you are working on a local workstation.

The majority of the steps were gleaned from [Building for Supported
Renesas
Boards](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/5_3_RCar_Gen_3/)
in the AGL documentation.  We repeat specific steps here to
demonstrate the **exact** steps used and where `meta-aws` was inserted
to the build procedure so you can take it into account when you build
your own.

**NOTE**: We are building with multimedia features that you might not
want to use and this will be called out during the procedure.

## Hardware used

- 1x Renesas R-Car H3 WS2.0, RTP0RC77951SKBX010SA00, S/N 2236
- 1x 5V 8A Adapter, 5.5mm barrel plug
- 1x 8GB SanDisk Ultra microSD
- 1x micro USB cable

The build result will be run from microSD. It will be slower than
running from eMMC, but less involved procedurally.

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

## Prepare, invoke, and flash the build

Due to the size of this build, the build environment was defined in
the AWS Cloud using a `c5.18xlarge` instance type.  Remember that in
the AWS Cloud you pay for what you use, so to be thrifty consider
stopping or terminating your instance after performing the build
tasks.

1. Launch the EC2 instance with the Ubuntu 18.04 AMI.
2. Login to the EC2 instance with `ssh` and prepare system updates.
   
   ```bash
   sudo apt-get update
   sudo apt-get -y upgrade
   ```
3. In the EC2 instance terminal, perform steps on the [Downloading AGL
   Software](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/2_Downloading_AGL_Software/)
       page.
   
   The command line steps we used are here.  Please reference the
   documentation if you would like an explanation of steps.
   
   ```bash
   export AGL_TOP=$HOME/AGL
   echo 'export AGL_TOP=$HOME/AGL' >> $HOME/.bashrc
   mkdir -p $AGL_TOP
   
   mkdir -p $HOME/bin
   export PATH=$HOME/bin:$PATH
   echo 'export PATH=$HOME/bin:$PATH' >> $HOME/.bashrc
   curl https://storage.googleapis.com/git-repo-downloads/repo > $HOME/bin/repo
   chmod a+x $HOME/bin/repo
   ```
   We will be using the Jumping Jellyfish release since it aligns with
   the Yocto Project **dunfell** LTS release.

   ```bash
   cd $AGL_TOP
   mkdir jellyfish
   cd jellyfish
   repo init -b jellyfish -m jellyfish_10.0.0.xml -u https://gerrit.automotivelinux.org/gerrit/AGL/AGL-repo
   repo sync
   ```

5. Now that we have the basics setup, there are some Renesas specific
   parts to organize.  On your local workstation, download the Renesas
   proprietary drivers by following the download instructions in the
   section [Downloading Proprietary
   Drivers](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/5_3_RCar_Gen_3/#1-downloading-proprietary-drivers).
   
   To identify the drivers you will need, invoke this command within
   the EC2 build ennvironment.

   ```bash
   grep -rn ZIP_.= $AGL_TOP/jellyfish/meta-agl/meta-agl-bsp/meta-rcar-gen3/scripts/setup_mm_packages.sh
   ```
   At the time of writing, the command output:
   
   ```text
   3:ZIP_1="R-Car_Gen3_Series_Evaluation_Software_Package_for_Linux-weston8-20191206.zip"
   4:ZIP_2="R-Car_Gen3_Series_Evaluation_Software_Package_of_Linux_Drivers-weston8-20191021.zip"
   ```

   Which are the v3.21.0 payloads.

   Please follow the rest of the steps to download from Renesas to
   your workstation (in this case it is for YP3.1 Dunfell), upload to
   the EC2 instance, and then configure.
   
   We did not try the non-MMP v4.1.0 version.

   From your workstation, upload the files. Replace the 'x' characters
   with real values according to your situation.
   
   ```bash
    scp -i ~/.ssh/xxx.pem ~/Downloads/R-Car*.zip ubuntu@xx.xxx.xxx.xxx:
   ```
   
   This is how we handled the placement of the files:
   
   ```bash
   ssh -i ~/.ssh/xxx.pem ubuntu@xx.xxx.xxx.xxx:
   mkdir XDG_DOWNLOAD_DIR
   mv R*zip XDG_DOWNLOAD_DIR
   export XDG_DOWNLOAD_DIR=$HOME/XDG_DOWNLOAD_DIR
   chmod a+x $XDG_DOWNLOAD_DIR/*.zip
   
   ```

4. In our build, we will be building for the AGL telematics demo in
meta-agl-telematics-demo. To set this up easily for the R-Car,
initialize the build environment with the new `aglsetup.sh` utility.
Its full use is described in the [Initializing Your Build Environment](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/3_Initializing_Your_Build_Environment/)
section of the AGL documentation.

   ```bash
   cd $AGL_TOP/jellyfish
   source meta-agl/scripts/aglsetup.sh \
       -m h3ulcb \
       -b h3ulcb \
       agl-demo agl-devel# AWS IoT Greengrass for the Renesas R-Car H3 and Automotive Grade Linux

You may perform the build steps on an EC2 instance, copy the image
locally, and then flash the MicroSD locally.  The steps in this
tutorial expects you are working on a local workstation.

The majority of the steps were gleaned from [Building for Supported
Renesas
Boards](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/5_3_RCar_Gen_3/)
in the AGL documentation.  We repeat specific steps here to
demonstrate the **exact** steps used and where `meta-aws` was inserted
to the build procedure so you can take it into account when you build
your own.

**NOTE**: We are building with multimedia features that you might not
want to use and this will be called out during the procedure.

## Hardware used

- 1x Renesas R-Car H3 WS2.0, RTP0RC77951SKBX010SA00, S/N 2236
- 1x 5V 8A Adapter, 5.5mm barrel plug
- 1x 8GB SanDisk Ultra microSD
- 1x micro USB cable

The build result will be run from microSD. It will be slower than
running from eMMC, but less involved procedurally.

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

## Prepare, invoke, and flash the build

Due to the size of this build, the build environment was defined in
the AWS Cloud using a `c5.18xlarge` instance type.  Remember that in
the AWS Cloud you pay for what you use, so to be thrifty consider
stopping or terminating your instance after performing the build
tasks.

1. Launch the EC2 instance with the Ubuntu 18.04 AMI.
2. Login to the EC2 instance with `ssh` and prepare system updates.
   
   ```bash
   sudo apt-get update
   sudo apt-get -y upgrade
   ```
3. In the EC2 instance terminal, perform steps on the [Downloading AGL
   Software](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/2_Downloading_AGL_Software/)
       page.
   
   The command line steps we used are here.  Please reference the
   documentation if you would like an explanation of steps.
   
   ```bash
   export AGL_TOP=$HOME/AGL
   echo 'export AGL_TOP=$HOME/AGL' >> $HOME/.bashrc
   mkdir -p $AGL_TOP
   
   mkdir -p $HOME/bin
   export PATH=$HOME/bin:$PATH
   echo 'export PATH=$HOME/bin:$PATH' >> $HOME/.bashrc
   curl https://storage.googleapis.com/git-repo-downloads/repo > $HOME/bin/repo
   chmod a+x $HOME/bin/repo
   ```
   We will be using the Jumping Jellyfish release since it aligns with
   the Yocto Project **dunfell** LTS release.

   ```bash
   cd $AGL_TOP
   mkdir jellyfish
   cd jellyfish
   repo init -b jellyfish -m jellyfish_10.0.0.xml -u https://gerrit.automotivelinux.org/gerrit/AGL/AGL-repo
   repo sync
   ```

5. Now that we have the basics setup, there are some Renesas specific
   parts to organize.  On your local workstation, download the Renesas
   proprietary drivers by following the download instructions in the
   section [Downloading Proprietary
   Drivers](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/5_3_RCar_Gen_3/#1-downloading-proprietary-drivers).
   
   To identify the drivers you will need, invoke this command within
   the EC2 build ennvironment.

   ```bash
   grep -rn ZIP_.= $AGL_TOP/jellyfish/meta-agl/meta-agl-bsp/meta-rcar-gen3/scripts/setup_mm_packages.sh
   ```
   At the time of writing, the command output:
   
   ```text
   3:ZIP_1="R-Car_Gen3_Series_Evaluation_Software_Package_for_Linux-weston8-20191206.zip"
   4:ZIP_2="R-Car_Gen3_Series_Evaluation_Software_Package_of_Linux_Drivers-weston8-20191021.zip"
   ```

   Which are the v3.21.0 payloads.

   Please follow the rest of the steps to download from Renesas to
   your workstation (in this case it is for YP3.1 Dunfell), upload to
   the EC2 instance, and then configure.
   
   We did not try the non-MMP v4.1.0 version.

   From your workstation, upload the files. Replace the 'x' characters
   with real values according to your situation.
   
   ```bash
    scp -i ~/.ssh/xxx.pem ~/Downloads/R-Car*.zip ubuntu@xx.xxx.xxx.xxx:
   ```
   
   This is how we handled the placement of the files:
   
   ```bash
   ssh -i ~/.ssh/xxx.pem ubuntu@xx.xxx.xxx.xxx:
   mkdir XDG_DOWNLOAD_DIR
   mv R*zip XDG_DOWNLOAD_DIR
   export XDG_DOWNLOAD_DIR=$HOME/XDG_DOWNLOAD_DIR
   chmod a+x $XDG_DOWNLOAD_DIR/*.zip
   
   ```

4. In our build, we will be building for the AGL telematics demo in
meta-agl-telematics-demo. To set this up easily for the R-Car,
initialize the build environment with the new `aglsetup.sh` utility.
Its full use is described in the [Initializing Your Build Environment](https://docs.automotivelinux.org/en/master/#0_Getting_Started/2_Building_AGL_Image/3_Initializing_Your_Build_Environment/)
section of the AGL documentation.

   ```bash
   cd $AGL_TOP/jellyfish
   source meta-agl/scripts/aglsetup.sh \
       -m h3ulcb \
       -b h3ulcb \
       agl-demo agl-devel

   cd $AGL_TOP/jellyfish/bsp/meta-renesas
   sh meta-rcar-gen3/docs/sample/copyscript/copy_evaproprietary_softwares.sh -f ${XDG_DOWNLOAD_DIR}
   ```

5. At this point, the base image is configured but we need to add AWS
   IoT Greengrass.
   
   ```bash
   cd $AGL_TOP/jellyfish
   git clone -b dunfell https://github.com/aws/meta-aws
   bitbake-layers add-layer $AGL_TOP/jellyfish/meta-aws
   ```
6. Edit the passwd and group file in meta-agl-profile-core to overcome
   https://github.com/aws/meta-aws/issues/75 

7. Add the following to local.conf by invoking this command.
   
   ```bash
   echo "IMAGE_INSTALL_append = \"greengrass openssh ntp\"" >> \
    $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   echo "MACHINE_FEATURES_append = \" multimedia\"" >> $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   echo "DISTRO_FEATURES_append = \" use_eva_pkg"\" >> $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   ```
   
8. Invoke the build.

   ```bash
   bitbake agl-demo-platform
   ```

9. Download files.  The dtb and Image files are in /boot in the
   tarball. Because of this, the Image and dtb load from the root file
   system.
   
   The .srec file download and application to QuickFlash or QSPI has
   to be done only once and is here for visibility.  For iterative
   builds, downloading the .tar.bz2 is sufficient.

```bash
cd ~/Downloads
BASEDIR=AGL/jellyfish/h3ulcb/tmp/deploy/images/h3ulcb
FILES="bootparam_sa0.srec
       bl2-h3ulcb.srec
       cert_header_sa6.srec
       bl31-h3ulcb.srec
       tee-h3ulcb.srec
       u-boot-elf-h3ulcb.srec
       agl-demo-platform-h3ulcb.tar.bz2"
for FILE in $FILES; do
    scp -i  ~/.ssh/xxx.pem ubuntu@xx.xxx.xxx.xx:$BASEDIR/$FILE .
done

```

10. Partition the microSD card which only has to be done once per
    microSD card.  Essentially, you will delete all partitions and
    create one large partition.
    
    **WARNING** Make sure you are using the microSD card you intend to
    partition by checking the device first with `lsblk`!

    ```bash
    sudo fdisk /dev/mmcblk0
    => (use 'd' to delete all partitions)
    => (use 'n' to create a new rootfs partition, taking all space)
    => (use 'l' and '83' to label the partition as Linux)
    => w
    ```

11. Create the partition and lay down the rootfs.  Important: do not
    forget to sync filesystems.

    ```bash
    sudo ext3.mkfs /dev/mmcblk0p1
    sudo mount /dev/mmcblk0p1 /mnt
    # note where your mmc is mounted
    sudo tar xfj agl-demo-platform-h3ulcb.tar.bz2 -C /mnt && sync
    ```

    **NOTE** for simplicity, at this point you can also manually
    provision your AWS IoT Greengrass credentials by copying them
    directly to the root file system.  This is not the way to
    provision in production which will be covered in a different
    tutorial.  If you are unsure of how to provision AWS IoT
    Greengrass credentials, see the IoT Greengrass documentation.

    Unmount the microSD, eject the microSD, and insert the microSD to
    the R-Car.

12. For iterative development, TFTP/NFS/microSD image provisioning is
    the most convenient.  In this case, we are using microSD so we
    need to change the U-Boot commands accordingly.  This has to be
    done only when you desire to change the boot target and any
    variant (kernel, dtb, etc).
    
    1. Connect over USB with your favorite terminal tool at
       115200/8/N/1.
    2. Press SW8 to reboot the device and press the space bar to stop
       automatic booting when U-Boot runs.
    3. Run the following commands at the U-Boot prompt.  These
       commands were taken from the Renesas / AGL guide and worked
       great.

       ```bash
=> setenv bootargs console=ttySC0,115200 ignore_loglevel vmalloc=384M video=HDMI-A-1:1920x1080-32@60 root=/dev/mmcblk1p1 rw rootfstype=ext4 rootwait rootdelay=2
=> setenv bootcmd run load_ker\; run load_dtb\; booti 0x48080000 - 0x48000000
=> setenv load_ker ext4load mmc 0:1 0x48080000 /boot/Image
=> setenv load_dtb ext4load mmc 0:1 0x48000000 /boot/r8a7795-h3ulcb.dtb
=> saveenv
       ```

Commission the network (already done if using Ethernet) and provision
the device with AWS Cloud credentials and AWS IoT Greengrass. See the
Greengrass documentation for more details.

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


## End notes

SMACK settings must me modified on the system.  In the near future
this will be fixed in meta-aws but right now it must be done
manually.  Also, the permissions will be narrowed.

Login to the r-car via serial and issue the following command:

```bash
sed -i 's/System _ -----l/System _ -wx--l/g' /etc/smack/accesses.d/default-access-domains
```

Reboot for the changes to go into effect.


© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.


   cd $AGL_TOP/jellyfish/bsp/meta-renesas
   sh meta-rcar-gen3/docs/sample/copyscript/copy_evaproprietary_softwares.sh -f ${XDG_DOWNLOAD_DIR}
   ```

5. At this point, the base image is configured but we need to add AWS
   IoT Greengrass.
   
   ```bash
   cd $AGL_TOP/jellyfish
   git clone -b dunfell https://github.com/aws/meta-aws
   bitbake-layers add-layer $AGL_TOP/jellyfish/meta-aws
   ```
6. Edit the passwd and group file in meta-agl-profile-core to overcome
   https://github.com/aws/meta-aws/issues/75 

7. Add the following to local.conf by invoking this command.
   
   ```bash
   echo "IMAGE_INSTALL_append = \"greengrass openssh ntp\"" >> \
    $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   echo "MACHINE_FEATURES_append = \" multimedia\"" >> $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   echo "DISTRO_FEATURES_append = \" use_eva_pkg"\" >> $AGL_TOP/jellyfish/h3ulcb/conf/local.conf
   ```
   
8. Invoke the build.

   ```bash
   bitbake agl-demo-platform
   ```

9. Download files.  The dtb and Image files are in /boot in the
   tarball. Because of this, the Image and dtb load from the root file
   system.
   
   The .srec file download and application to QuickFlash or QSPI has
   to be done only once and is here for visibility.  For iterative
   builds, downloading the .tar.bz2 is sufficient.

```bash
cd ~/Downloads
BASEDIR=AGL/jellyfish/h3ulcb/tmp/deploy/images/h3ulcb
FILES="bootparam_sa0.srec
       bl2-h3ulcb.srec
       cert_header_sa6.srec
       bl31-h3ulcb.srec
       tee-h3ulcb.srec
       u-boot-elf-h3ulcb.srec
       agl-demo-platform-h3ulcb.tar.bz2"
for FILE in $FILES; do
    scp -i  ~/.ssh/xxx.pem ubuntu@xx.xxx.xxx.xx:$BASEDIR/$FILE .
done

```

10. Partition the microSD card which only has to be done once per
    microSD card.  Essentially, you will delete all partitions and
    create one large partition.
    
    **WARNING** Make sure you are using the microSD card you intend to
    partition by checking the device first with `lsblk`!

    ```bash
    sudo fdisk /dev/mmcblk0
    => (use 'd' to delete all partitions)
    => (use 'n' to create a new rootfs partition, taking all space)
    => (use 'l' and '83' to label the partition as Linux)
    => w
    ```

11. Create the partition and lay down the rootfs.  Important: do not
    forget to sync filesystems.

    ```bash
    sudo ext3.mkfs /dev/mmcblk0p1
    sudo mount /dev/mmcblk0p1 /mnt
    # note where your mmc is mounted
    sudo tar xfj agl-demo-platform-h3ulcb.tar.bz2 -C /mnt && sync
    ```

    **NOTE** for simplicity, at this point you can also manually
    provision your AWS IoT Greengrass credentials by copying them
    directly to the root file system.  This is not the way to
    provision in production which will be covered in a different
    tutorial.  If you are unsure of how to provision AWS IoT
    Greengrass credentials, see the IoT Greengrass documentation.

    Unmount the microSD, eject the microSD, and insert the microSD to
    the R-Car.

12. For iterative development, TFTP/NFS/microSD image provisioning is
    the most convenient.  In this case, we are using microSD so we
    need to change the U-Boot commands accordingly.  This has to be
    done only when you desire to change the boot target and any
    variant (kernel, dtb, etc).
    
    1. Connect over USB with your favorite terminal tool at
       115200/8/N/1.
    2. Press SW8 to reboot the device and press the space bar to stop
       automatic booting when U-Boot runs.
    3. Run the following commands at the U-Boot prompt.  These
       commands were taken from the Renesas / AGL guide and worked
       great.

       ```bash
=> setenv bootargs console=ttySC0,115200 ignore_loglevel vmalloc=384M video=HDMI-A-1:1920x1080-32@60 root=/dev/mmcblk1p1 rw rootfstype=ext4 rootwait rootdelay=2
=> setenv bootcmd run load_ker\; run load_dtb\; booti 0x48080000 - 0x48000000
=> setenv load_ker ext4load mmc 0:1 0x48080000 /boot/Image
=> setenv load_dtb ext4load mmc 0:1 0x48000000 /boot/r8a7795-h3ulcb.dtb
=> saveenv
       ```

Commission the network (already done if using Ethernet) and provision
the device with AWS Cloud credentials and AWS IoT Greengrass. See the
Greengrass documentation for more details.

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


## End notes

SMACK settings must me modified on the system.  In the near future
this will be fixed in meta-aws but right now it must be done
manually.  Also, the permissions will be narrowed.

Login to the r-car via serial and issue the following command:

```bash
sed -i 's/System _ -----l/System _ -wx--l/g' /etc/smack/accesses.d/default-access-domains
```

Reboot for the changes to go into effect.


© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.


    **NOTE** for simplicity, at this point you can also manually
    provision your AWS IoT Greengrass credentials by copying them
    directly to the root file system.  This is not the way to
    provision in production which will be covered in a different
    tutorial.  If you are unsure of how to provision AWS IoT
    Greengrass credentials, see the IoT Greengrass documentation.

    Unmount the microSD, eject the microSD, and insert the microSD to
    the R-Car.

12. For iterative development, TFTP/NFS/microSD image provisioning is
    the most convenient.  In this case, we are using microSD so we
    need to change the U-Boot commands accordingly.  This has to be
    done only when you desire to change the boot target and any
    variant (kernel, dtb, etc).
    
    1. Connect over USB with your favorite terminal tool at
       115200/8/N/1.
    2. Press SW8 to reboot the device and press the space bar to stop
       automatic booting when U-Boot runs.
    3. Run the following commands at the U-Boot prompt.  These
       commands were taken from the Renesas / AGL guide and worked
       great.

       ```bash
=> setenv bootargs console=ttySC0,115200 ignore_loglevel vmalloc=384M video=HDMI-A-1:1920x1080-32@60 root=/dev/mmcblk1p1 rw rootfstype=ext4 rootwait rootdelay=2
=> setenv bootcmd run load_ker\; run load_dtb\; booti 0x48080000 - 0x48000000
=> setenv load_ker ext4load mmc 0:1 0x48080000 /boot/Image
=> setenv load_dtb ext4load mmc 0:1 0x48000000 /boot/r8a7795-h3ulcb.dtb
=> saveenv
       ```

Commission the network (already done if using Ethernet) and provision
the device with AWS Cloud credentials and AWS IoT Greengrass. See the
Greengrass documentation for more details.

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


## End notes

SMACK settings must me modified on the system.  In the near future
this will be fixed in meta-aws but right now it must be done
manually.  Also, the permissions will be narrowed.

Login to the r-car via serial and issue the following command:

```bash
sed -i 's/System _ -----l/System _ -wx--l/g' /etc/smack/accesses.d/default-access-domains
```

Reboot for the changes to go into effect.


© 2020, Amazon Web Services, Inc. or its affiliates. All rights reserved.
