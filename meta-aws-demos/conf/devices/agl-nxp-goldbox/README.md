# agl-nxp-goldbox
https://www.nxp.com/design/designs/goldbox-for-vehicle-networking-development-platform:GOLDBOX

Additional necessary packages to build:
```bash
sudo apt install mtools
```

```bash
export BUILD_DEVICE=agl-nxp-goldbox
bitbake aws-greengrass-test-image

or 
bitbake agl-image-boot

or
bitbake aws-biga-image

TODO:
      - cd $REPO_DIR
      - chmod +x ./gg-env-setup.sh
      - THING_NAME=agl-demo THING_GROUP_NAME=ew22 AWS_DEFAULT_REGION=eu-central-1 ./gg-env-setup.sh
      - cat ./local.conf >> $AGL_TOP/marlin/build/conf/local.conf
      - cp ./certs/* $AGL_TOP/marlin/meta-aws/recipes-iot/aws-iot-greengrass/files/
```
