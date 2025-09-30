FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://rauc.service.d/override.conf"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}/rauc.service.d
    install -m 0644 ${WORKDIR}/rauc.service.d/override.conf ${D}${systemd_system_unitdir}/rauc.service.d/
}

# enable debug logs for rauc to see stats of adaptive updates
FILES:${PN} += "${systemd_system_unitdir}/rauc.service.d/override.conf"
