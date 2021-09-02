#!/bin/bash

# following https://github.com/aws-samples/meta-aws-demos/blob/master/TDA4VMXEVM/aws_iot_greengrass/README.md
#
# and bits from http://arago-project.org/wiki/index.php/Setting_Up_Build_Environment
# 

export BASEDIR=$HOME

cd $BASEDIR/tisdk
./oe-layertool-setup.sh -f configs/arago-zeus-config.txt

cd $BASEDIR/tisdk/build
source conf/setenv

# patch bblayers.conf
sed '/^\"/i    \\t\/home/'$USER'/tisdk/sources/meta-aws \\' conf/bblayers.conf >conf/bblayers.1.conf
mv conf/bblayers.1.conf conf/bblayers.conf
 
 # assume toolchains are installed
export TOOLCHAIN_BASE=$BASEDIR/toolchains

export TOOLCHAIN_PATH_ARMV7=$BASEDIR/toolchains/gcc-arm-9.2-2019.12-x86_64-arm-none-linux-gnueabihf
export TOOLCHAIN_PATH_ARMV8=$BASEDIR/toolchains/gcc-arm-9.2-2019.12-x86_64-aarch64-none-linux-gnu

# and go...
cd $BASEDIR/tisdk/build
source conf/setenv
TOOLCHAIN_BASE=$BASEDIR/toolchains bitbake -r conf/j7-evm_iot_greengrass.conf arago-base-tisdk-image
