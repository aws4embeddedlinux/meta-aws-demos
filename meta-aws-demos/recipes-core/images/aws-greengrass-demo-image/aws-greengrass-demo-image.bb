SUMMARY = "A image to use test greengrass-bin"

IMAGE_INSTALL += "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

IMAGE_LINGUAS = " "

LICENSE = "MIT"

inherit core-image

IMAGE_ROOTFS_SIZE ?= "8192"

### AWS ###
IMAGE_INSTALL:append = " greengrass-bin"
