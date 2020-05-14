# Greengrass for UP2 board under Yocto

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

We need to add a couple of fixes from the master branch that are not integrated in the warrior branch yet.

```
cd meta-aws
git cherry-pick 1b91f16
git cherry-pick 49f2adb
git cherry-pick 3ba9e4e
```

Output:
```
$ git cherry-pick 1b91f16
[warrior 755b6ba] add support for greengrass 1.10.1
 Author: rpcme <rich@richelberger.com>
 Date: Wed May 6 14:25:26 2020 -0400
 1 file changed, 37 insertions(+)
 create mode 100644 recipes-greengrass/greengrass-core/greengrass_1.10.1.bb
$ git cherry-pick 49f2adb
[warrior cd93073] move runtime dependencies to individual version dists and change patchelf to target all x86_64
 Author: rpcme <rich@richelberger.com>
 Date: Wed May 6 16:42:22 2020 -0400
 3 files changed, 11 insertions(+), 8 deletions(-)
 delete mode 100644 recipes-greengrass/greengrass-core/greengrass_1.10.0.bbappend
$ git cherry-pick 3ba9e4e
[warrior a99ac0b] move runtime dependencies to version specific recipes since they will evolve over time
 Author: rpcme <rich@richelberger.com>
 Date: Wed May 6 16:45:39 2020 -0400
 1 file changed, 1 insertion(+), 2 deletions(-)
```

Apply a patch to add a symbolic link /lib64/ld-linux...  
Transfer the [ld-linux.patch](./ld-linux.patch) to the build system and apply:
```
patch -p1 ld-linux.patch
```


Rename to linux-yocto_%.bbappend file to linux-intel_%.bbappend and remove the linux-ti-staging_%.bbappend file because it is being picked up by bitbake.
```
cd meta-aws/recipes-greengrass/greengrass-core
cp base/linux-yocto_%.bbappend linux-intel_%.bbappend
cp base/linux-kernel.cfg .
cd ../../..
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
