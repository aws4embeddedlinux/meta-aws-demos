# qemuarm64 - qemu for arm64

This is the default DEVICE.

## build an qemuarm64 image

### Build the image

### Run this image in QEMU
```bash
runqemu slirp nographic
```

### Fix DNS (a bug, if not using systemd)
```bash
echo nameserver 8.8.4.4 >> /etc/resolv.conf
```
