SUMMARY = "GG obs"
DESCRIPTION = "GG obs"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

DEPENDS = "\
    aws-iot-device-sdk-cpp-v2 \
    fmt \
    "

### develop
inherit externalsrc 

EXTERNALSRC = "/home/ubuntu/data/git/GGObservabilityProject/GreengrassCore/artifacts/com.example.pubRTOSAppData/1.0.0"
EXTERNALSRC_BUILD = "${EXTERNALSRC}"

### develop

S = "${WORKDIR}/git"

inherit cmake

do_install() {
	install -d ${D}${bindir}
	install -m 0755 greengrass_pubRTOSAppData ${D}${bindir}
}

FILES_${PN} = "${bindir}/*"
