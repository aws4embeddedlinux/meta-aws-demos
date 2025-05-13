SUMMARY = "A demo image for aws-iot-greengrass-lite with A/B updates running on EC2"
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
IMAGE_INSTALL:append = " greengrass-lite"
IMAGE_INSTALL:append = " aws-iot-device-sdk-python-v2"
IMAGE_INSTALL:append = " jq"
IMAGE_INSTALL:append = " python3-misc python3-venv python3-tomllib python3-ensurepip libcgroup python3-pip"
IMAGE_INSTALL:append = " gdbserver"

# necessary to boot the system
IMAGE_INSTALL:append = " kernel-image"

### tmux ###
IMAGE_INSTALL:append = " tmux"
GLIBC_GENERATE_LOCALES = "en_US.UTF-8 UTF-8"
IMAGE_INSTALL:append = " glibc-utils localedef "
IMAGE_INSTALL:append = " ssh openssh-sshd openssh-sftp openssh-scp"

### misc ###
IMAGE_INSTALL:append = " sudo"

# this will disable root password - be warned!
# only for debugging - with this enabled you can login as root without password with the "EC2 serial console"
# Be aware that this is a security risk and cause a security warning mail!
# EXTRA_IMAGE_FEATURES += "serial-autologin-root empty-root-password allow-empty-password"
EXTRA_IMAGE_FEATURES += "serial-autologin-root empty-root-password allow-empty-password"

### license compliance ###
COPY_LIC_MANIFEST = "1"

COPY_LIC_DIRS = "1"

### debug tools ###
# IMAGE_INSTALL:append = " ldd gdb"
# IMAGE_INSTALL:append = " valgrind"
# IMAGE_INSTALL:append = " strace"
# IMAGE_INSTALL:append = " lsof"

# this will install all src, dbg packages to allow proper debugging with gdb
# EXTRA_IMAGE_FEATURES:append = " src-pkgs"
# EXTRA_IMAGE_FEATURES:append = " dbg-pkgs"

IMAGE_FEATURES += "read-only-rootfs"

# this should be equal to sdimage-aws-iot-greengrass-lite-demo-ab_partition.wks.in file,
# for rauc bundle generation wic file is not used!
IMAGE_PREPROCESS_COMMAND:append = " rootfs_user_fstab"

rootfs_user_fstab () {

install -d ${IMAGE_ROOTFS}/grubenv


# overwrite the default fstab, adding customization for this image
cat << EOF > ${IMAGE_ROOTFS}/${sysconfdir}/fstab
/dev/root            /                    auto       ro              1  1
proc                 /proc                proc       defaults              0  0
devpts               /dev/pts             devpts     mode=0620,ptmxmode=0666,gid=5      0  0
tmpfs                /run                 tmpfs      mode=0755,nodev,nosuid,strictatime 0  0
tmpfs                /var/volatile        tmpfs      defaults              0  0
LABEL=boot  /boot   vfat    defaults         0       0
LABEL=data     /data     ext4    x-systemd.growfs        0       0
LABEL=grubenv            /grubenv             auto       defaults,sync  0  0
/data/etc/ssh/               /etc/ssh/                 none    bind            0       0
/data/etc/systemd/system               /etc/systemd/system                 none    bind            0       0
/data/etc/systemd/network               /etc/systemd/network                 none    bind            0       0
/data/etc/greengrass                 /etc/greengrass                 none    bind            0       0
/data/var/lib/greengrass      /var/lib/greengrass      none    bind            0       0
/data/home      /home      none    bind            0       0
/data/root      /root      none    bind            0       0
EOF

install -d -m 0755 ${IMAGE_ROOTFS}/data

# copy those directories that should be present at the data partition to /data and just
# leave them empty as a mount point for the bind mount
install -d ${IMAGE_ROOTFS}/data/etc/greengrass
mv -f ${IMAGE_ROOTFS}/etc/greengrass/* ${IMAGE_ROOTFS}/data/etc/greengrass/


install -d -m 0755 ${IMAGE_ROOTFS}/data/root

install -d ${IMAGE_ROOTFS}/data/etc/systemd/system

install -d ${IMAGE_ROOTFS}/data/var/lib/greengrass

install -d ${IMAGE_ROOTFS}/data/home
mv -f ${IMAGE_ROOTFS}/home/* ${IMAGE_ROOTFS}/data/home/

install -d ${IMAGE_ROOTFS}/data/etc/systemd/network/

install -d ${IMAGE_ROOTFS}/data/etc/ssh/
mv -f ${IMAGE_ROOTFS}/etc/ssh/* ${IMAGE_ROOTFS}/data/etc/ssh/

# reload services after data partition is mounted
cat > ${IMAGE_ROOTFS}/lib/systemd/system/reload-systemd-data.service << 'EOF'
[Unit]
Description=Reload systemd services from data partition
After=data.mount

[Service]
Type=oneshot
ExecStart=/bin/systemctl daemon-reload
ExecStart=/bin/systemctl start --all
RemainAfterExit=yes
EOF

ln -sf /lib/systemd/system/reload-systemd-data.service ${IMAGE_ROOTFS}/lib/systemd/system/reload-systemd-data.service


# move all exising systemd services to the lib system directory and the remaining dir will be the bind mount point
cp -a ${IMAGE_ROOTFS}/etc/systemd/system/* ${IMAGE_ROOTFS}/lib/systemd/system/
rm -rf ${IMAGE_ROOTFS}/etc/systemd/system/*

# cloud-intit service needs to be started after data partition is mounted
mkdir -p ${IMAGE_ROOTFS}/lib/systemd/system/cloud-init.service.d/
cat << EOF > ${IMAGE_ROOTFS}/lib/systemd/system/cloud-init.service.d/overrides.conf
[Unit]
After=local-fs.target

[Service]
Restart=on-failure
RestartSec=30
StartLimitInterval=300
StartLimitBurst=3

[Install]
WantedBy=multi-user.target
EOF

# Allow user to use sudo
echo "user ALL=(ALL) NOPASSWD: ALL" >> ${IMAGE_ROOTFS}/etc/sudoers

}

####
do_image_wic[depends] += "boot-image:do_deploy"

RDEPENDS:${PN} += "grub-editenv e2fsprogs-mke2fs"

# Optimizations for RAUC adaptive method 'block-hash-index'
# rootfs image size must to be 4K-aligned
IMAGE_ROOTFS_ALIGNMENT = "4"

# ext4 block size should be set to 4K and use a fixed directory hash seed to
# reduce the image delta size (keep oe-core's 4K bytes-per-inode)
EXTRA_IMAGECMD:ext4 = "-i 4096 -b 4096 -E hash_seed=86ca73ff-7379-40bd-a098-fcb03a6e719d"

# for correct PVRE reporting
IMAGE_INSTALL:append = " curl amazon-ssm-agent util-linux"
IMAGE_FEATURES:append = " package-management"

# resize data partition to 100% of underlying device
IMAGE_INSTALL:append = " rauc-grow-data-part"

# size optimization
NO_RECOMMENDATIONS = "1"

IMAGE_INSTALL:append = " systemd-extra-utils"

# this will install the rauc configuration file
IMAGE_INSTALL:append = " virtual-rauc-conf"
