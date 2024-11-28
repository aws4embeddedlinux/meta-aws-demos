HOMEPAGE = "https://github.com/aws-greengrass/aws-greengrass-lite"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=34400b68072d710fecd0a2940a0d1658"

DEPENDS += "\
    curl \
    libevent \
    libyaml \
    openssl \
    sdbus-c++-libsystemd \
    sqlite3 \
    util-linux-libuuid \
    uriparser \
    libzip \
    "

DEPENDS:append:libc-musl = " argp-standalone"

LDFLAGS:append:libc-musl = " -largp"

### enable CLANG instead of GCC
#TOOLCHAIN = "clang"

###
# Use this for development to specify a local folder as source dir (cloned repo)
# inherit externalsrc
# EXTERNALSRC = "${TOPDIR}/../../aws-greengrass-lite"
# EXTERNALSRC_BUILD = "${EXTERNALSRC}/build/${DEVICE}_${IMAGE}"
###

#THIS IS DISABLED IF exernalsrc is enabled
SRC_URI = "\
    git://github.com/aws-greengrass/aws-greengrass-lite.git;protocol=https;branch=main \
    file://001-disable_strip.patch \
    file://greengrass-lite.yaml \
    file://run-ptest \
"

SRCREV = "94533996eeecb435740f6308b5ffe650eb07ee7a"
#
S = "${WORKDIR}/git"

FILES:${PN}:append = " \
    ${systemd_unitdir}/system/greengrass-lite.service \
    ${gg_rundir} \
    /usr/components/* \
    ${sysconfdir}/sudoers.d/${BPN} \
    /usr/lib/* \
    "

REQUIRED_DISTRO_FEATURES = "systemd"

do_configure[network] = "1"
EXTRA_OECMAKE:append = " -DFETCHCONTENT_FULLY_DISCONNECTED=OFF"

# default is stripped, we wanna do this by yocto
EXTRA_OECMAKE:append = " -DCMAKE_BUILD_TYPE=RelWithDebInfo"
# EXTRA_OECMAKE:append = " -DCMAKE_BUILD_TYPE=MinSizeRel"

# add DEBUG logs
EXTRA_OECMAKE:append = " -DGGL_LOG_LEVEL=DEBUG"
# EXTRA_OECMAKE:append = " -DGGL_LOG_LEVEL=TRACE"

# No warnings should be in commited code, not enabled yet
# CFLAGS:append = " -Werror"

SYSTEMD_SERVICE:${PN} = "\
    ggl.gg-ipc.socket.socket \
    ggl.core.ggipcd.service \
    ggl.gg_config.socket \
    ggl.core.ggconfigd.service \
    ggl.gg_health.socket \
    ggl.core.gghealthd.service \
    ggl.aws_iot_mqtt.socket \
    ggl.core.iotcored.service \
    ggl.gg_pubsub.socket \
    ggl.core.ggpubsubd.service \
    ggl.gg_deployment.socket \
    ggl.core.ggdeploymentd.service \
    ggl.gg_fleet_status.socket \
    ggl.core.gg-fleet-statusd.service \
    ggl.aws_iot_tes.socket \
    ggl.core.tesd.service \
    greengrass-lite.target \
"

inherit systemd cmake pkgconfig useradd features_check ptest

gg_workingdir = "${localstatedir}/lib/greengrass"
gg_user = "ggc_user"
gg_group = "ggc_group"

# set user and group for greengrass-lite
EXTRA_OECMAKE:append = " -DGGL_SYSTEMD_SYSTEM_USER=${gg_user}"
EXTRA_OECMAKE:append = " -DGGL_SYSTEMD_SYSTEM_GROUP=${gg_group}"
EXTRA_OECMAKE:append = " -DGGL_SYSTEMD_SYSTEM_DIR=${systemd_system_unitdir}"

do_install:append() {

    install -d ${D}/${gg_rundir}
    chown ${gg_user}:${gg_group} ${D}/${gg_rundir}

    install -d ${D}/${sysconfdir}/greengrass
    install -d -m 0755 ${D}/${sysconfdir}/greengrass/config.d

    install -m 0644 ${WORKDIR}/greengrass-lite.yaml ${D}/${sysconfdir}/greengrass/config.d
    sed -i -e 's,@GG_WORKING_DIR@,${gg_workingdir},g' \
            -e 's,@GG_USER@,${gg_user},g' \
            -e 's,@GG_GROUP@,${gg_group},g' \
            ${D}/${sysconfdir}/greengrass/config.d/greengrass-lite.yaml

    install -d ${D}/${gg_workingdir}
    chown ${gg_user}:${gg_group} ${D}/${gg_workingdir}
}

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r ${gg_group}"
USERADD_PARAM:${PN} = "-r -M -N -g  ${gg_group} -s /bin/false ${gg_user}"
