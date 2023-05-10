# AWS IoT Greengrass Nucelus for the Raspberry Pi 4
1. Build the image.

   ```bash
   export BUILD_DEVICE=rpi4-32
   bitbake core-image-minimal
   ```

   After building, the images will be in the following directory.

   ```bash
   ls tmp/deploy/images/raspberrypi4-64/*sdimg
   ```

    Where my image happens to be:

    ```bash
    FILE=tmp/deploy/images/raspberrypi4-32/core-image-minimal-raspberrypi4-32.rpi-sdimg
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

3. Modify config.txt

   In my case, I use the UART to communicate with the Raspberry Pi.  I
   then remove the remark for the `init_uart_baud` and
   `init_uart_clock` properties.
   

4. Eject the SD Card, insert the SD Card to the Raspberry Pi, connect
the UART and Ethernet, and power up.

