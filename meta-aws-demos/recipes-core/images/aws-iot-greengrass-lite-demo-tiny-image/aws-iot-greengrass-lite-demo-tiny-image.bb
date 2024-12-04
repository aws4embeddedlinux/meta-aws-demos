SUMMARY = "A tiny demo image for AWS IoT Greengrass lite"

IMAGE_INSTALL += "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

LICENSE = "MIT"

inherit core-image

### AWS ###
# disabled as there is a link issue
IMAGE_INSTALL:append = " greengrass-lite"

# only adding if device is rpi
IMAGE_INSTALL:append:rpi = " greengrass-config-init"

### MISC ###
IMAGE_INSTALL:append = " sudo"

# this will disable root password - be warned!
EXTRA_IMAGE_FEATURES ?= "debug-tweaks"

### license compliance ###
COPY_LIC_MANIFEST = "1"

COPY_LIC_DIRS = "1"