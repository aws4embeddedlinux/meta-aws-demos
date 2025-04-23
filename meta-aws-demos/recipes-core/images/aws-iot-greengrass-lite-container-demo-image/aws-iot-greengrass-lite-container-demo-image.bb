SUMMARY = "A demo image for gg-lite as a container"
DESCRIPTION = "A small systemd system container which will run greengrass-lite."

IMAGE_INSTALL += "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FEATURES += "empty-root-password allow-empty-password allow-root-login serial-autologin-root"

IMAGE_INSTALL:append = " python3-misc python3-venv python3-tomllib python3-ensurepip libcgroup python3-pip"
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
"

IMAGE_INSTALL:append = " systemd-serialgetty systemd-extra-utils systemd-conf"
# resolvconf

OCI_IMAGE_ENTRYPOINT = "/sbin/init systemd.unified_cgroup_hierarchy=1"

DISTRO_FEATURES:remove = "sysvinit"

### AWS ###
IMAGE_INSTALL:append = " greengrass-lite"
