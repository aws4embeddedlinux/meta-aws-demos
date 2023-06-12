# qemu x86-64

is the default BUILD_DEVICE

## build an qemux86-64 image with greengrass-bin and aws-iot-device-client installed

* Build the image 

```bash
bitbake aws-biga-image
```
* Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```