# agl-nxp-goldbox
https://www.nxp.com/design/designs/goldbox-for-vehicle-networking-development-platform:GOLDBOX

Additional necessary packages to build:
```bash
sudo apt install mtools
```

Build image
```bash
export BUILD_DEVICE=agl-nxp-goldbox
# e.g.
bitbake aws-biga-image
```

Tested images:
- [aws-biga-image](meta-aws-demos/recipes-core/images/aws-biga-image/README.md)



