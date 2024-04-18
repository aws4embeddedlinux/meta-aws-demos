# xilinx-zcu104-zynqmp

The whole image can be build with this (not working when your host machine is arm64!)
```bash
export DEVICE=xilinx-zcu104-zynqmp
bitbake aws-demo-image
runqemu slirp nographic

MACHINE=zynqmp-generic bitbake fsbl-firmware pmu-firmware
```
