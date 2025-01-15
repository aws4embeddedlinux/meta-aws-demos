DESCRIPTION = "A update bundle for aws-iot-greengrass-litewebrtc-demo-image"

inherit bundle

# RAUC_BUNDLE_VERSION = "v20200703"
# RAUC_BUNDLE_DESCRIPTION = "RAUC Demo Bundle"

RAUC_BUNDLE_SLOTS = "rootfs"
RAUC_SLOT_rootfs = "aws-iot-greengrass-lite-webrtc-demo-image"

RAUC_BUNDLE_COMPATIBLE   ?= "${MACHINE}"
RAUC_BUNDLE_FORMAT ?= "verity"

# those are the certs that are contained in meta-rauc-community/meta-rauc-raspberrypi
# they are intended for demo purpose only
RAUC_KEY_FILE ?= "${THISDIR}/files/development-1.key.pem"
RAUC_CERT_FILE ?= "${THISDIR}/files/development-1.cert.pem"
