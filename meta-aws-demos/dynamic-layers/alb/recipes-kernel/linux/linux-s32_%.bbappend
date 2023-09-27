FILESEXTRAPATHS:prepend:s32g274ardb2 := "${THISDIR}/${BPN}:"

COMPATIBLE_MACHINE:s32g274ardb2 = "s32g274ardb2"

# enable disable-can.patch by setting this in your config
# CONFIG:pn-linux-s32 = "add-disable-can-patch"
SRC_URI:append:s32g274ardb2 = " \
    ${@bb.utils.contains('CONFIG', 'add-disable-can-patch', 'file://0001-disable-can.patch', '', d)} \
"    
