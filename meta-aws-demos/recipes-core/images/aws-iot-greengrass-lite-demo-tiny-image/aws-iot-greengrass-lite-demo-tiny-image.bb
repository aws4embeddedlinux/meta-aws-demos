SUMMARY = "A tiny demo image for AWS IoT Greengrass lite"

IMAGE_INSTALL += "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

LICENSE = "MIT"

inherit core-image

### AWS ###
# disabled as there is a link issue
IMAGE_INSTALL:append = " greengrass-lite"
IMAGE_INSTALL:append = " python3-misc python3-venv python3-tomllib python3-ensurepip libcgroup python3-pip"

# only adding if device is rpi
IMAGE_INSTALL:append:rpi = " greengrass-config-init"

### MISC ###
IMAGE_INSTALL:append = " sudo"

# this will disable root password - be warned!
EXTRA_IMAGE_FEATURES ?= "allow-empty-password allow-root-login empty-root-password"

### license compliance ###
COPY_LIC_MANIFEST = "1"

COPY_LIC_DIRS = "1"

# this should be equal to sdimage-aws-iot-greengrass-lite-demo-ab_partition.wks.in file,
# for rauc bundle generation wic file is not used!
ROOTFS_POSTINSTALL_COMMAND += "extra_files"

extra_files () {
    # enable systemd-time-wait-sync as this is important for greengrass to have a correct clock
    ln -sf /${libdir}/systemd/system/systemd-time-wait-sync.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/
}
