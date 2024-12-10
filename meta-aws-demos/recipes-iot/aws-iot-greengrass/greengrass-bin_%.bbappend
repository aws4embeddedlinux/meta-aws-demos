# This are the necessary changes to make greengrass-bin using greengrass-config-init

FILESEXTRAPATHS:prepend:rpi := "${THISDIR}/greengrass-bin:"

SRC_URI:append:rpi = " \
    file://greengrass-classic.yaml \
    file://greengrass.service.template \
    "

gg_workingdir = "/greengrass/v2"
gg_user = "ggc_user"
gg_group = "ggc_group"

# fix service file - add systemd-time-wait-sync.service
do_install:append:rpi () {
    install -m 0640 ${S}/greengrass.service.template ${GG_ROOT}/packages/artifacts-unarchived/aws.greengrass.Nucleus/${PV}/aws.greengrass.nucleus/bin/greengrass.service.template
    # Install systemd service file
    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${S}/greengrass.service.template ${D}${systemd_unitdir}/system/greengrass.service
    sed -i -e "s,REPLACE_WITH_GG_LOADER_FILE,/${GG_BASENAME}/alts/current/distro/bin/loader,g" ${D}${systemd_unitdir}/system/greengrass.service
    sed -i -e "s,REPLACE_WITH_GG_LOADER_PID_FILE,/var/run/greengrass.pid,g" ${D}${systemd_unitdir}/system/greengrass.service

}

do_install:append:rpi () {
    install -m 0640 ${S}/greengrass-classic.yaml ${GG_ROOT}/greengrass-classic.yaml.fragment
    sed -i -e 's,@GG_WORKING_DIR@,${gg_workingdir},g' \
        -e 's,@GG_USER@,${gg_user},g' \
        -e 's,@GG_GROUP@,${gg_group},g' \
        ${GG_ROOT}/greengrass-classic.yaml.fragment
}

FILES:${PN}:append:rpi  = " \
    ${sysconfdir}/sudoers.d/${BPN} \
    "

do_install:append:rpi() {
    install -d -m 0750 ${D}/${sysconfdir}/sudoers.d
    echo "${gg_user} ALL=(root) NOPASSWD:/usr/bin/rauc *" >> ${D}/${sysconfdir}/sudoers.d/${BPN}
    chmod 0440 ${D}/${sysconfdir}/sudoers.d/${BPN}
}
