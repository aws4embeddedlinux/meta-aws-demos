FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://cloud-init.service"

do_install:append() {
    install -m 0644 ${WORKDIR}/cloud-init.service ${D}${systemd_system_unitdir}/cloud-init.service
}
