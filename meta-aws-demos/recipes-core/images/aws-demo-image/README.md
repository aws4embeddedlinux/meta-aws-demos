# aws-demo-image
Is a general demo installing all "useful" meta-aws packages into one image.

Development image features (`allow-empty-password`, `allow-root-login`, `empty-root-password`) are enabled by default, so no password is set for user root. These replace the `debug-tweaks` feature, which was split into its constituent features in Yocto 6.0 (wrynose).

When building for ec2-* remember that password login is disabled completly and only cert based ssh login with user `user` is configured by default.
