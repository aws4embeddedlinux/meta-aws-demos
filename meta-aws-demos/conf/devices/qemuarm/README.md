# qemuarm - qemu for arm32

## build an qemuarm image

### Build the image

### Run this image in QEMU
```bash
runqemu slirp nographic
```

### Fix DNS (a bug, if not using systemd)
```bash
echo nameserver 8.8.4.4 >> /etc/resolv.conf
```
