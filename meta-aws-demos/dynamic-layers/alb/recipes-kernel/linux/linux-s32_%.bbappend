FILESEXTRAPATHS:prepend:s32g274ardb2 := "${THISDIR}/${BPN}:"

COMPATIBLE_MACHINE:s32g274ardb2 = "s32g274ardb2"

SRC_URI:append:s32g274ardb2 = " \
    file://0001-disable-can.patch \
"
