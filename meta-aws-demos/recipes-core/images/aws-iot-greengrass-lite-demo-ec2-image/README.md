# aws-iot-greengrass-lite-demo-image

This image is similar to [aws-iot-greengrass-lite-demo-image](../aws-iot-greengrass-lite-demo-image/README.md)

The main difference it is made for using rauc on ec2 x86-64.

Tested with: [aws-ec2-x86-64](../../../conf/devices/aws-ec2-x86-64/)

## Build image and bundle

```bash
export DEVICE=aws-ec2-x86-64
export IMAGE=aws-iot-greengrass-lite-demo-image
bitbake $IMAGE
bitbake aws-iot-greengrass-lite-demo-ec2-bundle
```

## Image upload

More info here: [EC2 AMI creation feature](https://github.com/aws4embeddedlinux/meta-aws/blob/master/scripts/ec2-ami/README.md)

```bash
build$ ../layers/sw/meta-aws/scripts/ec2-ami/create-ec2-ami.sh amitest-bucket 16 aws-iot-greengrass-lite-demo-ec2-image aws-ec2-x86-64
```

## Greengrass lite configuration

Needs to be done manually at the moment, meaning creating config files and certs, keys using e.g. vi.

## Using Greengrass to deploy the rauc update bundle

Follow steps [here](../aws-iot-greengrass-lite-demo-image/README.md#demo-ab-update-greengrass-component).
