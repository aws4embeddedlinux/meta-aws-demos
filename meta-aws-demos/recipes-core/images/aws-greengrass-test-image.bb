SUMMARY = "A image to test meta-aws software"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

IMAGE_LINGUAS = " "

LICENSE = "MIT"

inherit core-image

IMAGE_ROOTFS_SIZE ?= "8192"
# IMAGE_ROOTFS_EXTRA_SPACE =+ "${@bb.utils.contains("DISTRO_FEATURES", "systemd", " + 4096", "", d)}"

### AWS ###

IMAGE_INSTALL =+ "greengrass-bin"
