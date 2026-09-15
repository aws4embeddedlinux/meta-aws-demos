SUMMARY = "Grub configuration file to use with RAUC"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

include conf/image-uefi.conf

RPROVIDES:${PN} += "virtual-grub-bootconf"

SRC_URI += " \
    file://grubenv \
    file://grub.cfg.in \
    "

# file:// sources unpack into ${UNPACKDIR}
S = "${UNPACKDIR}"

inherit deploy

do_install() {
        install -d ${D}${EFI_FILES_PATH}
            sed -e "s|@@KERNEL_IMAGE@@|${KERNEL_IMAGETYPE}|g" \
        ${UNPACKDIR}/grub.cfg.in > ${D}${EFI_FILES_PATH}/grub.cfg
}

FILES:${PN} += "${EFI_FILES_PATH}"

do_deploy() {
	install -m 644 ${UNPACKDIR}/grubenv ${DEPLOYDIR}

    sed -e "s|@@KERNEL_IMAGE@@|${KERNEL_IMAGETYPE}|g" \
        ${UNPACKDIR}/grub.cfg.in > ${DEPLOYDIR}/grub.cfg
}

addtask deploy after do_install before do_build
