#!/bin/bash

# Install system requirements
# target :  base Ubuntu 16.04 - Xenial

sudo apt-get update
sudo apt-get install -y git vim htop tree

# Ubunut 16.04 doesn't activate ssh by default... so install it
sudo apt-get install openssh-server -y

sudo apt-get install picocom -y

#
# TI Arago SDK install
#
#
# following https://github.com/aws-samples/meta-aws-demos/blob/master/TDA4VMXEVM/aws_iot_greengrass/README.md
#
# and bits from http://arago-project.org/wiki/index.php/Setting_Up_Build_Environment
# 

sudo apt-get install -y chrpath diffstat g++ 
sudo apt-get install -y texinfo

# sudo dpkg-reconfigure dash
echo "dash dash/sh boolean false" | sudo debconf-set-selections
sudo DEBIAN_FRONTEND=noninteractive dpkg-reconfigure dash

cd ~
export BASEDIR=$HOME
mkdir -p ~/toolchains 

wget https://developer.arm.com/-/media/Files/downloads/gnu-a/9.2-2019.12/binrel/gcc-arm-9.2-2019.12-x86_64-arm-none-linux-gnueabihf.tar.xz
tar xvf gcc-arm-9.2-2019.12-x86_64-arm-none-linux-gnueabihf.tar.xz -C $HOME/toolchains

wget https://developer.arm.com/-/media/Files/downloads/gnu-a/9.2-2019.12/binrel/gcc-arm-9.2-2019.12-x86_64-aarch64-none-linux-gnu.tar.xz
tar xvf gcc-arm-9.2-2019.12-x86_64-aarch64-none-linux-gnu.tar.xz -C $HOME/toolchains

cd $BASEDIR
git clone git://arago-project.org/git/projects/oe-layersetup.git tisdk

cd $BASEDIR/tisdk
./oe-layertool-setup.sh -f configs/arago-zeus-config.txt

cd build
source conf/setenv

cd $BASEDIR/tisdk/sources
git clone -b zeus https://github.com/aws/meta-aws

wget https://raw.githubusercontent.com/aws-samples/meta-aws-demos/master/TDA4VMXEVM/aws_iot_greengrass/j7-evm_iot_greengrass.conf
mv j7-evm_iot_greengrass.conf $BASEDIR/tisdk/build/conf/j7-evm_iot_greengrass.conf

export TOOLCHAIN_BASE=$BASEDIR/toolchains
export TOOLCHAIN_PATH_ARMV7=$BASEDIR/toolchains/gcc-arm-9.2-2019.12-x86_64-arm-none-linux-gnueabihf
export TOOLCHAIN_PATH_ARMV8=$BASEDIR/toolchains/gcc-arm-9.2-2019.12-x86_64-aarch64-none-linux-gnu
