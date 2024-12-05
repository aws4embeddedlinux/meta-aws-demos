# xilinx-zcu104-zynqmp

The whole image can be build with this (not working when your host machine is arm64!)
```bash
export DEVICE=xilinx-zcu104-zynqmp
runqemu slirp nographic

MACHINE=zynqmp-generic bitbake fsbl-firmware pmu-firmware
```

## You need to accept the license and add this to your config:
`LICENSE_FLAGS_ACCEPTED += "xilinx"`
