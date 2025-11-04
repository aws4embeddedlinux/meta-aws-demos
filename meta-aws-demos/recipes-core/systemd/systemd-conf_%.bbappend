do_install:append:rpi() {
    # enable watchdog on systemd configuration
    if ${@bb.utils.contains('MACHINE_FEATURES','watchdog','true','false',d)}; then
        install -d ${D}${systemd_unitdir}/system.conf.d/
        echo "[Manager]" > ${D}${systemd_unitdir}/system.conf.d/01-watchdog.conf
        echo "RuntimeWatchdogSec=60" >> ${D}${systemd_unitdir}/system.conf.d/01-watchdog.conf
        echo "ShutdownWatchdogSec=60" >> ${D}${systemd_unitdir}/system.conf.d/01-watchdog.conf
        echo "RebootWatchdogSec=60" >> ${D}${systemd_unitdir}/system.conf.d/01-watchdog.conf
    fi
}
