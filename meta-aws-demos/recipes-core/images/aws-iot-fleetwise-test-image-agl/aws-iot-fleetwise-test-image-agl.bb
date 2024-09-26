SUMMARY = "A image to test aws-iot-fleetwise"
inherit core-image

IMAGE_INSTALL += "tmux"

TOOLCHAIN_TARGET_TASK += "libwebsockets-dev mbedtls-dev"

TEST_SUITES = " ping aws-iot-fleetwise-edge "

IMAGE_INSTALL:append = " aws-iot-fleetwise-edge \
                            can-utils "
