SUMMARY = "A image to test meta-aws software"
inherit core-image

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

IMAGE_LINGUAS = " "

LICENSE = "MIT"

IMAGE_ROOTFS_SIZE ?= "8192"

### AWS ###

IMAGE_INSTALL =+ "greengrass-bin"

# IPCSHM ?= "ipc-shm"
# IMAGE_INSTALL:append:s32g = " ${IPCSHM} "
# IMAGE_INSTALL:append:s32r45evb = " ${IPCSHM} "

### test
# IMAGE_INSTALL =+ "gg-obs-ipc gg-obs-pub-can-data gg-obs-pub-rtos-app-data gg-obs-pub-rtos-os-data ipc-shm-us"

# allow obs to run
IMAGE_INSTALL =+ "aws-iot-device-sdk-cpp-v2 python3-pyserial fmt"

# 500MB
IMAGE_ROOTFS_EXTRA_SPACE = "524288"

# Enable deployment of rtos firmware to 'sdcard' image
# SDCARDIMAGE_BOOT_EXTRA_FILES:append:s32g = " install-rtos-image:rtos.image"

# use  this env config u-boot config for the sdcard
UBOOT_ENV_NAME:s32g = "u-boot-biga"

EXTRA_IMAGE_FEATURES += "debug-tweaks"

INIT_MANAGER = "systemd"
