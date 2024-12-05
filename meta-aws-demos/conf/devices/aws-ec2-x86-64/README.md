# aws-ec2-x86-64

Shows [EC2 AMI generation](https://github.com/aws4embeddedlinux/meta-aws/blob/master/scripts/ec2-ami/README.md) feature.

## build an aws-ec2-x86-64 image and create an EC2 AMI from it

### Set the DEVICE
```bash
export DEVICE=aws-ec2-x86-64
```

### Set the IMAGE (e.g. aws-demo-image)
```bash
export IMAGE=aws-demo-image
```

### Build the IMAGE
```bash
bitbake $IMAGE
```

### Upload this image to your ec2 account (follow instructions to setup this up before!)
```bash
cd ..
meta-aws-demos$ layers/sw/meta-aws/scripts/ec2-ami/create-ec2-ami.sh amitest-bucket 16 aws-demo-image aws-ec2-x86-64
```
