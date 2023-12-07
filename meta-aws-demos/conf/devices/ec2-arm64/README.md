# ec2-arm64

Shows [ec2 AMI generation](https://github.com/aws4embeddedlinux/meta-aws/blob/master/scripts/ec2-ami/README.md) features.

## build an ec2-arm64 image with greengrass-bin installed and create an EC2 AMI

* Build the image

```bash
export DEMO=ec2-arm64
bitbake aws-demo-image
```
* Upload this image to your ec2 account (follow instructions to set this up before!)
```bash
cd ..
meta-aws-demos$ layers/sw/meta-aws/scripts/ec2-ami/create-ec2-ami.sh amitest-bucket 16 aws-demo-image aws-ec2-arm64
```