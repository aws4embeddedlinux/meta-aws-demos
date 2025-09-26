DESCRIPTION = "A swupdate image for aws-iot-greengrass-lite-demo-image"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit swupdate

SRC_URI = "\
    file://emmcsetup.lua \
    file://sw-description \
"

# images to build before building swupdate image
IMAGE_DEPENDS = "aws-iot-greengrass-lite-demo-swupdate-image"

# images and files that will be included in the .swu image
SWUPDATE_IMAGES = "aws-iot-greengrass-lite-demo-swupdate-image-raspberrypi-armv8"
SWUPDATE_IMAGES_FSTYPES[aws-iot-greengrass-lite-demo-swupdate-image-raspberrypi-armv8] = ".rootfs.ext4.gz"
