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

EXTERNALSRC = "/home/ubuntu/data/git/GGObservabilityProject/GreengrassCore/artifacts/com.example.pubRTOSOSData/1.0.0"
EXTERNALSRC_BUILD = "${EXTERNALSRC}"

### develop

# enable PACKAGECONFIG = "static" to build static instead of shared
PACKAGECONFIG[static] = "-DBUILD_SHARED_LIBS=OFF ,-DBUILD_SHARED_LIBS=ON,,"

S = "${WORKDIR}/git"

inherit cmake

do_install() {
	install -d ${D}${bindir}
	install -m 0755 greengrass_pubRTOSOSdata ${D}${bindir}
}

FILES_${PN} = "${bindir}/*"
