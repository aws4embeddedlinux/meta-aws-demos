SUMMARY = "A demo image for aws-iot-greengrass with A/B updates"
HOMEPAGE = "https://github.com/aws4embeddedlinux/meta-aws-demos"

LICENSE = "MIT"

# this needs to be done before installing the (dynamic) packagegroups
inherit core-image

IMAGE_INSTALL += "\
    ${CORE_IMAGE_EXTRA_INSTALL} \
    packagegroup-base \
    packagegroup-core-boot \
    "

### AWS ###
IMAGE_INSTALL:append = " greengrass-bin udev"
IMAGE_INSTALL:append = " aws-iot-device-sdk-python-v2"
IMAGE_INSTALL:append = " python3-misc python3-venv python3-tomllib python3-ensurepip libcgroup python3-pip"

### rauc ###
CORE_IMAGE_EXTRA_INSTALL:append = " rauc-grow-data-part"

# only adding if device is rpi, as others might have a different partition layout
IMAGE_INSTALL:append:rpi = " greengrass-config-init yq"
# this will allow kernel updates with rauc
IMAGE_INSTALL:append = " kernel-image kernel-modules"

### tmux ###
IMAGE_INSTALL:append = " tmux"
GLIBC_GENERATE_LOCALES = "en_US.UTF-8 UTF-8"
IMAGE_INSTALL:append = " glibc-utils localedef "
IMAGE_INSTALL:append = " ssh openssh-sshd openssh-sftp openssh-scp"

### aws-iot-device-client ###
# IMAGE_INSTALL:append = " aws-iot-device-client"

### amazon-cloudwatch-publisher ###
# IMAGE_INSTALL:append = " amazon-cloudwatch-publisher"

### misc ###
IMAGE_INSTALL:append = " sudo"

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
IMAGE_FEATURES += "read-only-rootfs"

# this should be equal to sdimage-aws-iot-greengrass-lite-demo-ab_partition.wks.in file,
# for rauc bundle generation wic file is not used!
IMAGE_PREPROCESS_COMMAND:append = " rootfs_user_fstab"

rootfs_user_fstab () {

# overwrite the default fstab, adding customization for this image
cat << EOF > ${IMAGE_ROOTFS}/${sysconfdir}/fstab
/dev/root            /                    auto       defaults              1  1
proc                 /proc                proc       defaults              0  0
devpts               /dev/pts             devpts     mode=0620,ptmxmode=0666,gid=5      0  0
tmpfs                /run                 tmpfs      mode=0755,nodev,nosuid,strictatime 0  0
tmpfs                /var/volatile        tmpfs      defaults              0  0
LABEL=boot  /boot   vfat    defaults         0       0
LABEL=data     /data     ext4    x-systemd.growfs        0       0
/data/etc/wpa_supplicant             /etc/wpa_supplicant             none    bind            0       0
/data/etc/systemd/network            /etc/systemd/network            none    bind            0       0
/data/greengrass/v2/     /greengrass/v2/      none    bind            0       0
/data/home/     /home      none    bind            0       0
EOF

install -d -m 0755 ${IMAGE_ROOTFS}/data

# copy those directories that should be present at the data partition to /data and just
# leave them empty as a mount point for the bind mount

install -d ${IMAGE_ROOTFS}/data/etc/wpa_supplicant
# empty dir
# mv -f ${IMAGE_ROOTFS}/etc/wpa_supplicant/* ${IMAGE_ROOTFS}/data/etc/wpa_supplicant/

install -d ${IMAGE_ROOTFS}/data/etc/systemd/network
mv -f ${IMAGE_ROOTFS}/etc/systemd/network/* ${IMAGE_ROOTFS}/data/etc/systemd/network

install -d ${IMAGE_ROOTFS}/data/greengrass/v2
mv -f ${IMAGE_ROOTFS}/greengrass/v2/* ${IMAGE_ROOTFS}/data/greengrass/v2

# decided to do here instead of a bbappend of wpa:supplicant
install -d ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/
ln -sf /${libdir}/systemd/system/wpa_supplicant@.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/wpa_supplicant@wlan0.service

mv -f ${IMAGE_ROOTFS}/etc/hostname ${IMAGE_ROOTFS}/data/etc/hostname
ln -sf /data/etc/hostname ${IMAGE_ROOTFS}/etc/hostname

mv -f ${IMAGE_ROOTFS}/etc/hosts ${IMAGE_ROOTFS}/data/etc/hosts
ln -sf /data/etc/hosts ${IMAGE_ROOTFS}/etc/hosts

# enable systemd-time-wait-sync as this is important for greengrass to have a correct clock
ln -sf /${libdir}/systemd/system/systemd-time-wait-sync.service ${IMAGE_ROOTFS}/${sysconfdir}/systemd/system/multi-user.target.wants/

install -d ${IMAGE_ROOTFS}/data/home
# mv -f ${IMAGE_ROOTFS}/home/* ${IMAGE_ROOTFS}/data/home/
}