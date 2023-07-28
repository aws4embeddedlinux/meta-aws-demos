SUMMARY = "A image to test meta-aws software"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

IMAGE_LINGUAS = " "

LICENSE = "MIT"

inherit core-image

IMAGE_ROOTFS_SIZE ?= "8192"

### AWS ###

IMAGE_INSTALL =+ "greengrass-bin"

# IPCSHM ?= "ipc-shm"
# IMAGE_INSTALL:append:s32g = " ${IPCSHM} "
# IMAGE_INSTALL:append:s32r45evb = " ${IPCSHM} "

### test
# IMAGE_INSTALL =+ "gg-obs-ipc gg-obs-pub-can-data gg-obs-pub-rtos-app-data gg-obs-pub-rtos-os-data ipc-shm-us"