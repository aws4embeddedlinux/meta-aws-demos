# qemuarm64

is the default DEVICE

## build an qemuarm64 image

* Build the image

* Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```

* Fix DNS (a bug)
```bash
echo nameserver 8.8.4.4 >> /etc/resolv.conf
```