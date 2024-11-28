SUMMARY = "AWS IoT Greengrass config init"
DESCRIPTION = "AWS IoT Greengrass config init by zip from fat partition, e.g. for Raspberry Pi"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
    file://greengrass-config-init.service \
    file://greengrass-config-init.sh \
    file://wlan.network \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/greengrass-config-init.service \
    ${sysconfdir}/systemd/network/wlan.network \
"

RDEPENDS:${PN} += "\
    avahi-daemon \
    avahi-utils \
    sed \
    zip \
    "

inherit systemd features_check

REQUIRED_DISTRO_FEATURES = "wifi"

SYSTEMD_SERVICE:${PN} += "greengrass-config-init.service"

do_install() {
    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/greengrass-config-init.sh ${D}${bindir}/

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/greengrass-config-init.service ${D}${systemd_unitdir}/system/
    sed -i  -e 's,@BINDIR@,${bindir},g' \
            ${D}${systemd_unitdir}/system/greengrass-config-init.service

    install -d -m 0755 ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/wlan.network ${D}${sysconfdir}/systemd/network/

    install -d ${D}${sysconfdir}/wpa_supplicant
}
