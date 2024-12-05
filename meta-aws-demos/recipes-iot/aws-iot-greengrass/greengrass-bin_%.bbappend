FILESEXTRAPATHS:prepend:rpi := "${THISDIR}/greengrass-bin:"

SRC_URI:append:rpi = " \
    file://greengrass-classic.yaml \
    "

gg_workingdir = "/greengrass/v2"
gg_user = "ggc_user"
gg_group = "ggc_group"

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
