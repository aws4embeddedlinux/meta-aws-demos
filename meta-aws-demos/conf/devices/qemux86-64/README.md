# qemu x86-64

Use of integrated qemu for x86-64

* Run this image in QEMU. (root password disabled)
```bash
runqemu slirp nographic
```

* Fix DNS (a bug)
```bash
echo nameserver 8.8.4.4 >> /etc/resolv.conf
```