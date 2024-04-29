SUMMARY = "A image to test amazon-kvs-webrtc-sdk"
inherit core-image

IMAGE_INSTALL += "amazon-kvs-webrtc-sdk"

IMAGE_INSTALL += "gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-ugly tmux"

TOOLCHAIN_TARGET_TASK += "libwebsockets-dev mbedtls-dev"


TEST_SUITES = " ping aws-iot-fleetwise-edge "

QEMU_USE_KVM = ""
QEMU_USE_SLIRP = "1"

IMAGE_INSTALL:append = " aws-iot-fleetwise-edge \
                            can-utils "

CERTIFICATE:pn-aws-iot-fleetwise-edge="-----BEGIN-----\nXXXXX\n-----END-----\n"
PRIVATE_KEY:pn-aws-iot-fleetwise-edge="-----BEGIN-----\nXXXXX\n-----END-----\n"
VEHICLE_NAME:pn-aws-iot-fleetwise-edge="v1"
ENDPOINT_URL:pn-aws-iot-fleetwise-edge="xxx.iot.region.amazonaws.com"
CAN_BUS:pn-aws-iot-fleetwise-edge="vcan0"

# systemd
DISTRO_FEATURES:append = " largefile ptest multiarch systemd"
VIRTUAL-RUNTIME_init_manager = "systemd"
VIRTUAL-RUNTIME_initscripts = ""
VIRTUAL-RUNTIME_syslog = ""

QB_MEM = "-m 2048"