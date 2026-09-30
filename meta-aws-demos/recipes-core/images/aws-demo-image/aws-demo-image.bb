SUMMARY = "A image to test meta-aws software"
inherit core-image

IMAGE_INSTALL =+ "\
    amazon-cloudwatch-publisher \
    amazon-kvs-producer-pic \
    amazon-kvs-producer-sdk-c \
    amazon-kvs-producer-sdk-cpp \
    amazon-kvs-webrtc-sdk \
    amazon-ssm-agent \
    aws-c-auth \
    aws-c-cal \
    aws-c-common \
    aws-c-compression \
    aws-c-event-stream \
    aws-c-http \
    aws-c-io \
    aws-c-iot \
    aws-c-mqtt \
    aws-c-s3 \
    aws-c-sdkutils \
    aws-checksums \
    aws-cli \
    aws-crt-cpp \
    aws-crt-python \
    aws-iot-device-sdk-cpp-v2 \
    aws-iot-device-sdk-python-v2 \
    aws-iot-fleetwise-edge \
    aws-iot-securetunneling-localproxy \
    aws-sdk-cpp \
    corretto-11-bin \
    greengrass-bin \
    python3-boto3 \
    python3-botocore \
    python3-s3transfer \
    python3-timeloop \
    s2n \
    "

EXTRA_IMAGE_FEATURES += "allow-empty-password allow-root-login empty-root-password tools-debug"
