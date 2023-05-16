# qemu x86-64

is the default BUILD_DEVICE

## build an qemux86-64 image with greengrass-bin and aws-iot-device-client installed

* Build the image 

```
bitbake aws-greengrass-test-image
```
* Run this image in QEMU. (root password disabled)
```
runqemu slirp nographic
```