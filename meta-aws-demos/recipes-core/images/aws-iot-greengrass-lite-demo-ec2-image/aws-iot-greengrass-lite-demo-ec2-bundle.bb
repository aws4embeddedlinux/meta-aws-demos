DESCRIPTION = "A update bundle for aws-iot-greengrass-lite-demo-ec2-image"

inherit bundle

RAUC_BUNDLE_VERSION = "v20250522"
RAUC_BUNDLE_DESCRIPTION = "aws-iot-greengrass-lite-demo-ec2-image update bundle"

# RAUC_BUNDLE_SLOTS = "efi rootfs"
RAUC_BUNDLE_SLOTS = "rootfs"
RAUC_SLOT_rootfs = "aws-iot-greengrass-lite-demo-ec2-image"

RAUC_BUNDLE_COMPATIBLE   ?= "${MACHINE}"
RAUC_BUNDLE_FORMAT ?= "verity"

# those are the certs that are contained in meta-rauc-community/meta-rauc-raspberrypi
# they are intended for demo purpose only
RAUC_KEY_FILE ?= "${THISDIR}/files/development-1.key.pem"
RAUC_CERT_FILE ?= "${THISDIR}/files/development-1.cert.pem"

RAUC_IMAGE_FSTYPE = "tar.bz2"

# uncomment for enabling adaptive update method 'block-hash-index'
RAUC_SLOT_rootfs[fstype] = "ext4"
RAUC_SLOT_rootfs[adaptive] = "block-hash-index"
