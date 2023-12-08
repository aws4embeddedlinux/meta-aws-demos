# aws-demo-image
Is a general demo installing all "useful" meta-aws packages into one image.

`debug-tweaks` are enabled by default and therefor is no password set for user root.

When building for ec2-* remember that password login is disabled completly and only cert based ssh login with user `user` is configured by default.
