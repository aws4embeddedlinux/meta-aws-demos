# Shared configuration for images that install more than one consumer of the AWS
# CRT stack (aws-crt-cpp, aws-sdk-cpp, aws-iot-device-sdk-cpp-v2, aws-iot-
# fleetwise-edge, ...). require this from an image's config.conf.
#
# By default aws-crt-cpp (and aws-iot-device-sdk-cpp-v2) bundle their own copies
# of the CRT libraries via the "build-deps" PACKAGECONFIG. That bundled build
# conflicts with the standalone aws-c-*/s2n/aws-checksums packages an image also
# installs, and its bundled cmake config is internally inconsistent (config files
# reference shared/ targets while the bundled libs are built static), which fails
# any consumer that find_package()s the CRT chain (e.g. aws-sdk-cpp ->
# aws_use_package(aws-crt-cpp)).
#
# Disabling build-deps makes everything link the single shared standalone CRT
# stack. Only "build-deps" is removed; other PACKAGECONFIG entries (e.g. ptest's
# "with-tests") are preserved.
# aws-crt-cpp bundled cmake defect tracked:
# https://github.com/aws4embeddedlinux/meta-aws/issues/17161
PACKAGECONFIG:remove:pn-aws-crt-cpp = "build-deps"
PACKAGECONFIG:remove:pn-aws-iot-device-sdk-cpp-v2 = "build-deps"

# UPSTREAM WORKAROUND (drop once fixed): aws-iot-device-sdk-cpp-v2's BUILD_DEPS=OFF
# path is incomplete -- it find_package()s the CRT stack but neither DEPENDS on it
# nor sets CMAKE_PREFIX_PATH, so do_configure fails ("Could not find ... aws-crt-cpp
# / aws-c-common"). Supply both (single CMAKE_PREFIX_PATH prefix, no ';' list, to
# avoid shell splitting in run.do_configure). Inert for images that do not build
# the SDK. Tracked: https://github.com/aws4embeddedlinux/meta-aws/issues/17160
DEPENDS:append:pn-aws-iot-device-sdk-cpp-v2 = " \
    aws-crt-cpp \
    aws-c-auth \
    aws-c-cal \
    aws-c-common \
    aws-c-compression \
    aws-c-event-stream \
    aws-c-http \
    aws-c-io \
    aws-c-mqtt \
    aws-c-s3 \
    aws-c-sdkutils \
    aws-checksums \
    s2n \
"
EXTRA_OECMAKE:append:pn-aws-iot-device-sdk-cpp-v2 = " -DCMAKE_PREFIX_PATH=${STAGING_DIR_HOST}${prefix}"
