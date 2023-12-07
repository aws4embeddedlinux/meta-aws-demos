# qemu x86-64

is the default DEMO

## build an qemux86-64 image with greengrass-bin installed

* Build the image

```bash
bitbake aws-demo-image
```
* Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```