# qemu x86-arm64

is the default DEVICE

## build an qemux86-64 image with greengrass-bin installed

* Build the image

* Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```

* Fix DNS (a bug)
```bash
echo nameserver 8.8.4.4 >> /etc/resolv.conf
```