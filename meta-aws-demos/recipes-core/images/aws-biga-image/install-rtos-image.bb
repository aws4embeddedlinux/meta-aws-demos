SUMMARY = "take S32 FreeRTOS image to install it on sdcard image"
LICENSE = "CLOSED"

SRC_URI = "file://rtosPROD.elf"

S = "${WORKDIR}"

FILES:${PN} += "\
    rtosPROD.elf \
    "

inherit deploy

COMPATIBLE_MACHINE = "s32g"

# Deploy FW for u-boot
do_deploy() {
	install -d ${DEPLOY_DIR_IMAGE}
	install -m 0644 rtosPROD.elf ${DEPLOY_DIR_IMAGE}/rtos.image
}

addtask do_deploy after do_install

# avoid "QA Issue: Architecture did not match" caused by firmware
INSANE_SKIP:${PN} += "arch already-stripped"
