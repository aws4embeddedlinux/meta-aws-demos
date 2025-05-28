IMAGE_INSTALL:append = " kernel-image kernel-modules"

inherit aws-ec2-image
WKS_FILE = "ec2_partition.wks.in"
do_image_wic[depends] += "boot-image:do_deploy"

# Optimizations for RAUC adaptive method 'block-hash-index'
# rootfs image size must to be 4K-aligned
IMAGE_ROOTFS_ALIGNMENT = "4"
# ext4 block size should be set to 4K and use a fixed directory hash seed to
# reduce the image delta size (keep oe-core's 4K bytes-per-inode)
EXTRA_IMAGECMD:ext4 = "-i 4096 -b 4096 -E hash_seed=86ca73ff-7379-40bd-a098-fcb03a6e719d"

IMAGE_PREPROCESS_COMMAND:append = " rootfs_user_fstab"

####
rootfs_user_fstab () {
    install -d -m 0755 ${IMAGE_ROOTFS}/data
}