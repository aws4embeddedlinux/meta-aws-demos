# biga-qemu
run biga demo on qemu with virtual CAN support

## Build image
```bash
export DEMO=biga-qemu
# e.g.
bitbake aws-biga-image
```

## RUN QEMU and create can0 interface in the qemu biga image
```bash
runqemu slirp nographic qemuparams="-object can-bus,id=canbus0 -object can-host-socketcan,id=canhost0,if=vcan0,canbus=canbus0  -device kvaser_pci,canbus=canbus0"
#(with kvm enabled) runqemu kvm slirp nographic qemuparams="-object can-bus,id=canbus0 -object can-host-socketcan,id=canhost0,if=vcan0,canbus=canbus0  -device kvaser_pci,canbus=canbus0"
ip link set can0 type can bitrate 1000000
ip link set up can0

candump can0
```

## Setup vcan on the build machine
```bash
sudo apt-get install -y linux-modules-extra-$(uname -r)
modprobe vcan
ip link add dev vcan0 type vcan
ip link set up vcan0
```

### Send a can message to the vcan0 interface
If the qemu is up and running it should receive it.
```bash
cansend vcan0 123#00FFAA5501020304
```

## additional information
https://www.pragmaticlinux.com/2021/10/how-to-create-a-virtual-can-interface-on-linux/


## Tested images:
- [aws-biga-image](/meta-aws-demos/recipes-core/images/aws-biga-image/README.md)
