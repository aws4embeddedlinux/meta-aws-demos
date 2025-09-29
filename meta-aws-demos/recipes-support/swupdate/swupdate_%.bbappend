do_install:append() {
    sed -i "s#raspberryPI#${MACHINE}#g" ${D}${sysconfdir}/swupdate.cfg
}

# we do not use swupdate.sh
SYSTEMD_AUTO_ENABLE = "disable"