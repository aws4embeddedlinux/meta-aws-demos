SUMMARY = "AWS IoT Greengrass lite"
DESCRIPTION = "AWS IoT Greengrass runtime for constrained devices"
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
    git://github.com/aws-greengrass/aws-greengrass-lite.git;protocol=https;branch=main;name=ggl \
    git://github.com/FreeRTOS/coreMQTT.git;protocol=https;branch=main;name=mqtt;destsuffix=${S}/thirdparty/core_mqtt \
    git://github.com/FreeRTOS/backoffAlgorithm.git;protocol=https;branch=main;name=backoff;destsuffix=${S}/thirdparty/backoff_algorithm \
    git://github.com/aws/SigV4-for-AWS-IoT-embedded-sdk.git;protocol=https;branch=main;name=sigv4;destsuffix=${S}/thirdparty/aws_sigv4 \
    git://github.com/aws-greengrass/aws-greengrass-sdk-lite.git;protocol=https;branch=main;name=sdk;destsuffix=${S}/thirdparty/ggl_sdk \
    file://001-disable_strip.patch \
    file://greengrass-lite.yaml \
    file://run-ptest \
"

SRCREV_ggl = "649d4d4946b5c0f558470117ff542c86f0b53d7b"

# must match fc_deps.json
SRCREV_mqtt = "f1827d8b46703f1c5ff05d21b34692d3122c9a04"
SRCREV_backoff = "f2f3bb2d8310f7cb48baa3ee64b635a5d66f838b"
SRCREV_sigv4 = "f0409ced6c2c9430f0e972019b7e8f20bbf58f4e"
SRCREV_sdk = "0d239f96101608441dd6434f98a9e7f6623556c7"

EXTRA_OECMAKE:append = " \
    -DFETCHCONTENT_SOURCE_DIR_CORE_MQTT=${S}/thirdparty/core_mqtt \
    -DFETCHCONTENT_SOURCE_DIR_BACKOFF_ALGORITHM=${S}/thirdparty/backoff_algorithm \
    -DFETCHCONTENT_SOURCE_DIR_AWS_SIGV4=${S}/thirdparty/aws_sigv4 \
    -DFETCHCONTENT_SOURCE_DIR_GGL_SDK=${S}/thirdparty/ggl_sdk \
    "

SRCREV_FORMAT .= "_ggl_core_mqtt_backoff_aws_sigv4_ggl_sdk"

do_configure:prepend() {
    # verify that all dependencies have correct version
    grep -q ${SRCREV_mqtt} ${S}/fc_deps.json || bbfatal "ERROR: dependency version mismatch, please update 'SRCREV_mqtt'!"
    grep -q ${SRCREV_backoff} ${S}/fc_deps.json || bbfatal "ERROR: dependency version mismatch, please update 'SRCREV_backoff'!"
    grep -q ${SRCREV_sigv4} ${S}/fc_deps.json || bbfatal "ERROR: dependency version mismatch, please update 'SRCREV_sigv4'!"
    grep -q ${SRCREV_sdk} ${S}/fc_deps.json || bbfatal "ERROR: dependency version mismatch, please update 'SRCREV_sdk'!"
}

S = "${WORKDIR}/git"

FILES:${PN}:append = " \
    ${systemd_unitdir}/system/greengrass-lite.service \
    /usr/components/* \
    ${sysconfdir}/sudoers.d/${BPN} \
    /usr/lib/* \
    ${gg_workingdir} \
    "

REQUIRED_DISTRO_FEATURES = "systemd"

# enable fleetprovisioning for testing by default to get test coverage
PACKAGECONFIG ?= "\
    ${@bb.utils.contains('PTEST_ENABLED', '1', 'with-tests', '', d)} \
    ${@bb.utils.contains('PTEST_ENABLED', '1', 'fleetprovisioning', '', d)} \
    "

# this is to make the PACKAGECONFIG QA check happy
PACKAGECONFIG[fleetprovisioning] = ""

PACKAGECONFIG[with-tests] = "-DBUILD_TESTING=ON -DBUILD_EXAMPLES=ON,-DBUILD_TESTING=OFF,"

# default is stripped, we wanna do this by yocto
EXTRA_OECMAKE:append = " -DCMAKE_BUILD_TYPE=RelWithDebInfo"
# EXTRA_OECMAKE:append = " -DCMAKE_BUILD_TYPE=MinSizeRel"

# add DEBUG logs
EXTRA_OECMAKE:append = " -DGGL_LOG_LEVEL=DEBUG"
# EXTRA_OECMAKE:append = " -DGGL_LOG_LEVEL=TRACE"

# No warnings should be in commited code, not enabled yet
# CFLAGS:append = " -Werror"

SYSTEMD_SERVICE:${PN} = "\
    ggl.aws_iot_mqtt.socket \
    ggl.aws_iot_tes.socket \
    ggl.aws.greengrass.TokenExchangeService.service \
    ggl.core.gg-fleet-statusd.service \
    ggl.core.ggconfigd.service \
    ggl.core.ggdeploymentd.service \
    ggl.core.gghealthd.service \
    ggl.core.ggipcd.service \
    ggl.core.ggpubsubd.service \
    ggl.core.iotcored.service \
    ggl.core.tesd.service \
    ggl.gg_config.socket \
    ggl.gg_deployment.socket \
    ggl.gg_fleet_status.socket \
    ggl.gg_health.socket \
    ggl.gg_pubsub.socket \
    ggl.gg-ipc.socket.socket \
    ggl.ipc_component.socket \
    greengrass-lite.target \
"

inherit systemd cmake pkgconfig useradd features_check ptest

gg_workingdir ?= "${localstatedir}/lib/greengrass"

# https://github.com/aws-greengrass/aws-greengrass-lite/blob/main/docs/INSTALL.md#usergroup
# user and group for greengrass itself
gg_user = "ggcore"
gg_group = "ggcore"

# default user and group for greengrass components
ggc_user = "gg_component"
ggc_group = "gg_component"

# set user and group for greengrass-lite itself
EXTRA_OECMAKE:append = " -DGGL_SYSTEMD_SYSTEM_USER=${gg_user}"
EXTRA_OECMAKE:append = " -DGGL_SYSTEMD_SYSTEM_GROUP=${gg_group}"
EXTRA_OECMAKE:append = " -DGGL_SYSTEMD_SYSTEM_DIR=${systemd_system_unitdir}"

do_install:append() {

    install -d ${D}/${sysconfdir}/greengrass
    install -d -m 0755 ${D}/${sysconfdir}/greengrass/config.d

    install -m 0644 ${WORKDIR}/greengrass-lite.yaml ${D}/${sysconfdir}/greengrass/config.d
    sed -i -e 's,@GG_WORKING_DIR@,${gg_workingdir},g' \
            -e 's,@GG_USER@,${gg_user},g' \
            -e 's,@GG_GROUP@,${gg_group},g' \
            ${D}/${sysconfdir}/greengrass/config.d/greengrass-lite.yaml

    install -d ${D}/${gg_workingdir}
    chown ${gg_user}:${gg_group} ${D}/${gg_workingdir}

    if ${@bb.utils.contains('PACKAGECONFIG','fleetprovisioning','true','false',d)}; then
    echo TODO

#        install -d ${GG_ROOT}/claim-certs
#        install -d ${GG_ROOT}/plugins
#        install -d ${GG_ROOT}/plugins/trusted
#        install -m 0440 ${WORKDIR}/claim.pkey.pem ${GG_ROOT}/claim-certs
#        install -m 0440 ${WORKDIR}/claim.cert.pem ${GG_ROOT}/claim-certs
#        install -m 0440 ${WORKDIR}/claim.root.pem ${GG_ROOT}/claim-certs
#
#        install -m 0740 ${WORKDIR}/fleetprovisioningbyclaim-${GGV2_FLEETPROVISIONING_VERSION}.jar ${GG_ROOT}/plugins/trusted/aws.greengrass.FleetProvisioningByClaim.jar
#
#        install -m 0755 ${WORKDIR}/replace_board_id.sh ${GG_ROOT}/config/
#
#        install -m 0640 ${WORKDIR}/config.yaml.template ${GG_ROOT}/config/config.yaml
#
#        AWS_DEFAULT_REGION=${GGV2_REGION} \
#        PROXY_USER=ggc_user:ggc_group \
#        IOT_DATA_ENDPOINT=${GGV2_DATA_EP} \
#        IOT_CRED_ENDPOINT=${GGV2_CRED_EP} \
#        TE_ROLE_ALIAS=${GGV2_TES_RALIAS} \
#        FLEET_PROVISIONING_TEMPLATE_NAME=${GGV2_FLEET_PROVISIONING_TEMPLATE_NAME} \
#        CLAIM_CERT_PATH=/${GG_BASENAME}/claim-certs/claim.cert.pem \
#        CLAIM_KEY_PATH=/${GG_BASENAME}/claim-certs/claim.pkey.pem \
#        ROOT_CA_PATH=/${GG_BASENAME}/claim-certs/claim.root.pem \
#        THING_NAME=${GGV2_THING_NAME} \
#        THING_GROUP_NAME=${GGV2_THING_GROUP} \
#        envsubst < ${WORKDIR}/config.yaml.template > ${GG_ROOT}/config/config.yaml

    fi

}

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r ${gg_group}; -r ${ggc_group}"
USERADD_PARAM:${PN} = "-r -M -N -g  ${gg_group} -s /bin/false ${gg_user}; -r -M -N -g  ${ggc_group} -s /bin/false ${ggc_user}"
