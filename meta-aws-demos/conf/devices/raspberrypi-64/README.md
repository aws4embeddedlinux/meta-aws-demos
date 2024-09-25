# AWS IoT Greengrass Nucelus for the Raspberry Pi 64
1. Build the image.

   ```bash
   export DEVICE=raspberrypi-64
   bitbake aws-demo-image
   ```

   After building, the images will be in the following directory.

   ```bash
   ls tmp/deploy/images/raspberrypi-armv8/*sdimg
   ```

    Where my image happens to be:

    ```bash
    FILE=tmp/deploy/images/raspberrypi-armv8/core-image-minimal-raspberrypi-armv8.rpi-sdimg
    ```

2. Image the target device using `dd`.  You can also use an imaging
   tool you are comfortable with. **BE SUPER CAREFUL**

   First identify the device using `lsblk` and then set it to the
   output variable. This will help you confirm that this is really
   the target device you want to image. **BE SUPER CAREFUL**

   ```bash
   lsblk
   #DEVICE=<IDENTIFIED DEVICE, i.e.>
   DEVICE=/dev/sda
   sudo dd if=$FILE of=$DEVICE bs=1m
   ```


3. Eject the SD Card, insert the SD Card to the Raspberry Pi, connect
the UART and Ethernet, and power up.
