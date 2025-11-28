FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://rauc.service.d/override.conf"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}/rauc.service.d
    install -m 0644 ${WORKDIR}/rauc.service.d/override.conf ${D}${systemd_system_unitdir}/rauc.service.d/
}

do_install:append:rpi() {
    # Remove ExecStart line for mmcblk0
    sed -i '/ExecStart=.*mmcblk0.*resizepart 6/d' ${D}${systemd_system_unitdir}/rauc-grow-data-partition.service
}

# enable debug logs for rauc to see stats of adaptive updates
FILES:${PN}:append = " ${systemd_system_unitdir}/rauc.service.d/override.conf"
