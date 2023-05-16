# ec2-x86-64

Shows [ec2 AMI generation](https://github.com/aws4embeddedlinux/meta-aws/blob/master/scripts/ec2-ami/README.md) features.

## build an ec2-x86-64 image with greengrass-bin installed and create an EC2 AMI

* Build the image 

```bash
bitbake aws-greengrass-test-image
```
* Upload this image to your ec2 account (follow instructions to set this up before!)
```bash
cd ..
meta-aws-demos$ layers/sw/meta-aws/scripts/ec2-ami/create-ec2-ami.sh amitest-bucket 16 aws-greengrass-test-image aws-ec2-x86-64
```