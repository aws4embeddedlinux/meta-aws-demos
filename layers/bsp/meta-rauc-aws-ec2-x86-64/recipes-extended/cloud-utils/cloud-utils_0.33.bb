LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "git://github.com/canonical/cloud-utils;protocol=https;branch=main"

SRCREV = "646ab04dcc275565608af3acc2f27ad8ca79dcfe"

RDEPENDS:${PN} += " \
	python3-core \
	bash \
"
S = "${WORKDIR}/git"

do_compile () {
	oe_runmake
}

do_install () {
	oe_runmake install 'DESTDIR=${D}'
}
