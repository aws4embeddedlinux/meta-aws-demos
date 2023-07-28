SUMMARY = "Support for Inter-Process(or) Communication over Shared Memory (ipc-shm-us) userspace lib"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

# TODO
#  Could not resolve host: source.codeaurora.org
# URL ?= "gitsm://github.com/nxp-auto-linux/ipc-shm-us;protocol=https"
# BRANCH ?= "${RELEASE_BASE}"
# SRC_URI = "\
#             ${URL};branch=${BRANCH} \
#             file://Makefile \
#             "
# SRCREV = "c1513e2f3ae3f94d9bfe2885232bfe0b50aaa3a1"

### develop
inherit externalsrc

EXTERNALSRC = "/home/ubuntu/data/git/GGObservabilityProject/GreengrassCore/artifacts/com.example.IPC/1.0.0/ipc-shm-us/"
EXTERNALSRC_BUILD = "${EXTERNALSRC}"

### develop


# SRC_URI += "file://Makefile"

S = "${WORKDIR}/git"
DESTDIR="${D}"

PLATFORM_FLAVOR:s32g2 = "s32g2"
PLATFORM_FLAVOR:s32g3 = "s32g3"
PLATFORM_FLAVOR:s32r45evb = "s32r45"
EXTRA_OEMAKE:append = " PLATFORM_FLAVOR=${PLATFORM_FLAVOR} "

DEPENDS += "ipc-shm"
# TODO find correct variable for getting 5.15.96-rt61+gf2b25660adcf string
export IPC_UIO_MODULE_DIR="${PKG_CONFIG_SYSROOT_DIR}/lib/modules/5.15.96-rt61+gf2b25660adcf/extra/"
export PLATFORM="S32GEN1"

# do_configure:prepend () {
#     cp ${WORKDIR}/Makefile ${S}/Makefile 
# }
do_install () {
    install -d 0644 ${D}/${datadir}/lib
    cp libipc-shm.a ${D}/${datadir}/lib
}

FILES:${PN}-staticdev += "/usr/share/lib/libipc-shm.a"