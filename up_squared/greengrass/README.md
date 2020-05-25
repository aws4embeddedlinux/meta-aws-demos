# Greengrass for UP2 board with Yocto

For more information on building the Yocto image for UP2 board, see:  https://github.com/AAEONAEU-SW/meta-up-board. The warrior branch is used because it has python3.7 support.


## Downloading the base BSP layer

```
git clone -b warrior git://git.yoctoproject.org/poky.git
cd poky
git clone -b warrior git://git.yoctoproject.org/meta-intel.git
git clone -b warrior git://git.openembedded.org/meta-openembedded 
git clone -b warrior git://git.yoctoproject.org/meta-virtualization
git clone -b warrior git://git.openembedded.org/openembedded-core
git clone -b warrior-dev https://github.com/emutex/meta-up-board
```

## Adding the Java and AWS layers

```
git clone -b warrior https://github.com/aws/meta-aws
git clone -b warrior https://git.yoctoproject.org/git/meta-java
```

## Applying a couple of patches to make it compatible with the UP2 BSP

This tutorial has been verified with meta-aws commit ID ae0e7b9. If there would be breaking changes in the future, please revert to this commit:
```
cd meta-aws
git checkout ae0e7b9
```

Since the UP2 board is using the linux-intel kernel, we have to copy to linux-yocto_%.bbappend file to linux-intel_%.bbappend.
```
cd meta-aws/recipes-greengrass/greengrass-core/base
cp linux-yocto_%.bbappend linux-intel_%.bbappend
cd ../../../..
```

## Configure the BSP

```
TEMPLATECONF=meta-up-board/conf source oe-init-build-env
bitbake-layers add-layer ../meta-aws
bitbake-layers add-layer ../meta-java
```

Add greengrass and java to the image.
Edit the `conf/local.cfg` file and add the following line at the end:
```
IMAGE_INSTALL_append = " greengrass openjdk-8"
```

## Build the image
```
MACHINE=up-squared bitbake upboard-image-sato
```
the resulting image is `tmp/deploy/images/up-squared/upboard-image-sato-up-squared.hddimg`.  
Transfer this image to a USB stick using dd (or rufus on windows), as desribed in the Aaeon [tutorial](https://github.com/AAEONAEU-SW/meta-up-board).

## Configuring Greengrass on the target
Once the target is running, configure the Greengrass agent with the security resources as described in the GG tutorial here: https://docs.aws.amazon.com/greengrass/latest/developerguide/gg-config.html. There is no need to download the Greengrass Core software, since it is already pre-installed in the Yocto image.  


Summarized, these 2 actions still need to done:
```
tar -xzvf {hash}-setup.tar.gz -C /greengrass
wget -O /greengrass/certs/root.ca.pem https://www.amazontrust.com/repository/AmazonRootCA1.pem
```

To retart the greengrass daemon with the configured credentials:  
`systemctl restart greengrass`

To check wether the Greengrass service is running:  
`systemctl status greengrass`

Once the greengrass daemon is running, you may continue the AWS Greengrass tutorial at module 3: https://docs.aws.amazon.com/greengrass/latest/developerguide/module3-I.html

## Debugging

To view the greengrass logfile: `tail -f /greengrass/ggc/var/log/system/runtime.log`
