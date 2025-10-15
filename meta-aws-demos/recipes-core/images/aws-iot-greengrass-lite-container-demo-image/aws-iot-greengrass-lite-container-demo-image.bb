SUMMARY = "A demo image for gg-lite as a container"
DESCRIPTION = "A small systemd system container which will run greengrass-lite."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FEATURES += "empty-root-password allow-empty-password allow-root-login read-only-rootfs"
IMAGE_FEATURES:remove = "package-management doc-pkgs"

# Remove locale data
IMAGE_LINGUAS = ""

PACKAGE_CLASSES = "package_ipk"

IMAGE_INSTALL:append = " libcgroup"
IMAGE_INSTALL:remove = " packagegroup-core-base-utils shadow shadow-base"
IMAGE_INSTALL:remove = "openssh vim gawk iproute2 coreutils e2fsprogs-e2fsck e2fsprogs-mke2fs e2fsprogs-tune2fs perl"

# Force exclude packages
PACKAGE_EXCLUDE += "coreutils vim gawk iproute2 e2fsprogs-e2fsck e2fsprogs-mke2fs e2fsprogs-tune2fs packagegroup-core-ssh-openssh"

# Use minimal providers
PREFERRED_PROVIDER_coreutils = "busybox"
PREFERRED_PROVIDER_util-linux = "busybox"
# Use local.conf to specify additional systemd services to disable. To overwrite
# the default list use SERVICES_TO_DISABLE:pn-systemd-container in local.conf
SERVICES_TO_DISABLE = "systemd-userdbd.service"
SERVICES_TO_DISABLE:append = " ${SYSTEMD_CONTAINER_DISABLE_SERVICES}"

# Use local.conf to enable systemd services
SERVICES_TO_ENABLE += "${SYSTEMD_CONTAINER_ENABLE_SERVICES}"

require container-systemd-base.inc

SYSTEMD_CONTAINER_DISABLE_SERVICES += " \
    systemd-resolved.service \
    var-volatile.mount \
    systemd-udevd.service \
    systemd-hwdb-update.service \
    systemd-modules-load.service \
    systemd-vconsole-setup.service \
"

IMAGE_INSTALL:append = " systemd-serialgetty systemd-conf"
# resolvconf

OCI_IMAGE_ENTRYPOINT = "/sbin/init systemd.unified_cgroup_hierarchy=1"

DISTRO_FEATURES:remove = "sysvinit"

### AWS ###
IMAGE_INSTALL:append = " greengrass-lite"

# disable fleetprovisioning
PACKAGECONFIG:pn-greengrass-lite = ""

# Disable unnecessary systemd features for containers
PACKAGECONFIG:pn-systemd:remove = "backlight hibernate hostnamed localed machined networkd resolved rfkill timesyncd timedated vconsole"

# Set root password to "root"
ROOTFS_POSTPROCESS_COMMAND += "set_root_passwd; remove_extra_files;"
set_root_passwd() {
    echo 'root:root' | chpasswd -R ${IMAGE_ROOTFS}
}

# Remove unnecessary files to reduce image size
remove_extra_files() {
    rm -rf ${IMAGE_ROOTFS}/usr/share/common-licenses
    rm -rf ${IMAGE_ROOTFS}/usr/share/keymaps
    rm -rf ${IMAGE_ROOTFS}/usr/share/misc
}


IMAGE_INSTALL:append = " systemd systemd-serialgetty"