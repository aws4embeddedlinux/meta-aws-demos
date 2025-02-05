SUMMARY = "A demo image for aws-iot-greengrass"
HOMEPAGE = "https://github.com/aws4embeddedlinux/meta-aws-demos"

LICENSE = "MIT"

# this needs to be done before installing the (dynamic) packagegroups
inherit core-image

IMAGE_ROOTFS_SIZE ?= "8192"
IMAGE_INSTALL += "\
    ${CORE_IMAGE_EXTRA_INSTALL} \
    packagegroup-base \
    packagegroup-core-boot \
    "
### AWS ###
IMAGE_INSTALL:append = " greengrass-bin udev"
IMAGE_INSTALL:append = " python3-misc python3-venv python3-tomllib python3-ensurepip libcgroup python3-pip"

# only adding if device is rpi, as others might have a different partition layout
IMAGE_INSTALL:append:rpi = " greengrass-config-init yq"

### tmux ###
IMAGE_INSTALL:append = " tmux"
GLIBC_GENERATE_LOCALES = "en_US.UTF-8 UTF-8"
IMAGE_INSTALL:append = " glibc-utils localedef "
IMAGE_INSTALL:append = " openssh-ssh openssh-sshd openssh-sftp openssh-scp"

### aws-iot-device-client ###
# IMAGE_INSTALL:append = " aws-iot-device-client"

### amazon-cloudwatch-publisher ###
# IMAGE_INSTALL:append = " amazon-cloudwatch-publisher"

### misc ###
IMAGE_INSTALL:append = " sudo "

# this will disable root password - be warned!
EXTRA_IMAGE_FEATURES ?= "allow-empty-password allow-root-login empty-root-password"

### license compliance ###
COPY_LIC_MANIFEST = "1"

COPY_LIC_DIRS = "1"

### debug tools ###
# IMAGE_INSTALL:append = " ldd gdb"
# IMAGE_INSTALL:append = " valgrind"
# IMAGE_INSTALL:append = " strace"


# this will install all src, dbg packages to allow proper debugging with gdb
# EXTRA_IMAGE_FEATURES:append = " src-pkgs dbg-pkgs"

# this should be equal to sdimage-aws-iot-greengrass-lite-demo-ab_partition.wks.in file,
# for rauc bundle generation wic file is not used!
ROOTFS_POSTPROCESS_COMMAND += "extra_files"

extra_files () {
    # decided to do here instead of a bbappend of wpa:supplicant
    install -d ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/
    ln -sf ${libdir}/systemd/system/wpa_supplicant@.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/wpa_supplicant@wlan0.service

    # enable systemd-time-wait-sync as this is important for greengrass to have a correct clock
    ln -sf /${libdir}/systemd/system/systemd-time-wait-sync.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/
}