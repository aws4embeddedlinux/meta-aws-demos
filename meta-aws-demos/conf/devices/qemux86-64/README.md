# qemux86-64 - qemu for x86-64

## build an qemux86-64 image

### Build the image

### Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```

### Fix DNS (a bug, if not using systemd)
```bash
echo nameserver 8.8.4.4 >> /etc/resolv.conf
```
