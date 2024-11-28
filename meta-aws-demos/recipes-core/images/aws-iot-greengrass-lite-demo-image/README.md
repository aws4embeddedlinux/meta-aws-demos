# aws-iot-greengrass-lite-demo-ab-image
A/B update example made with [meta-rauc](https://github.com/rauc/meta-rauc-community)

Set IMAGE to aws-iot-greengrass-lite-demo-ab-image.
```
export IMAGE=aws-iot-greengrass-lite-demo-ab-image
```

This image works only with raspberry pi. cause of bootloader settings.
Setting DEVICE to raspberrypi-64
```
export DEVICE=raspberrypi-64
```

First compile, enable local use of openssl
```
bitbake openssl-native -caddto_recipe_sysroot
```

Build the image
```
bitbake $IMAGE
```

Build the update bundle - the update that can be applied to the image.
```
bitbake aws-iot-greengrass-lite-demo-ab-bundle
```

Flash the image onto your device e.g.
Be careful device depends on your setup - may sda is your harddisk and not a sd card!!!
You can also extract this and write it with rpi-imager!
```
bzcat aws-iot-greengrass-lite-demo-ab-image-raspberrypi-armv8.rootfs.wic.bz2 | sudo dcfldd of=/dev/sda
```

Then power-on the board and log in. To see that RAUC is configured correctly and can interact with the bootloader, run:
```
rauc status
```

To install an upgrade bundle manually just exec on the device
```
rauc install <URL>
```

To swith manually the slot
```
rauc status mark-active other
```

The rootfs is read only, for development purpose you can mount it read writeable
```
mount -o remount,rw /
```
