# aws-iot-greengrass-lite-webrtc-demo-image

This image contain [amazon-kvs-webrtc-sdk](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c) and when started it automatically connect to Amazon Kinesis Video Streams WebRTC using the [kvsWebrtcClientMasterGstSample](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c?tab=readme-ov-file#sample-kvswebrtcclientmastergstsample) that use the default camera and microphone to init a WebRTC connection as a Master.
It is based on [aws-iot-greengrass-lite-demo-image](../aws-iot-greengrass-lite-demo-image/README.md) and does have the same features, such as A/B update, read only partition etc.

The channel name is the same as the thing name. It will use AWS IoT certs to connect to your account. [For this permissions need to be added to your account.](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c?tab=readme-ov-file#setup-iot) E.g. Attach this policy to your: GreengrassV2CoreDeviceRole (when using the connection kit)

```json
{
   "Version":"2012-10-17",
   "Statement":[
      {
          "Effect":"Allow",
          "Action":[
            "kinesisvideo:DescribeSignalingChannel",
            "kinesisvideo:CreateSignalingChannel",
            "kinesisvideo:GetSignalingChannelEndpoint",
            "kinesisvideo:GetIceServerConfig",
            "kinesisvideo:ConnectAsMaster"
          ],
          "Resource":"arn:aws:kinesisvideo:*:*:channel/${credentials-iot:ThingName}/*"
      }
   ]
}
```
Note: if you want to use keys on your device you need to edit [here](aws-iot-greengrass-lite-webrtc-demo-image.bb#L123) and [here](config.conf#L66).

### Create a signalling channel in web console or cli (can also done on testpage)

```bash
aws kinesisvideo create-signaling-channel \
    --channel-name "<ThingName>" \
    --channel-type SINGLE_MASTER
```

## Bulid image - tested on raspberry pi zero with camera module V3.

```bash
. init-build-env
export DEVICE=raspberrypi-64
export IMAGE=aws-iot-greengrass-lite-webrtc-demo-image
bitbake $IMAGE
```

## Flash onto your sd card - be careful about device naming to not overwrite the wrong disk!

```bash
sudo bzcat build/tmp/deploy/images/raspberrypi-armv8/aws-iot-greengrass-lite-webrtc-demo-image-raspberrypi-armv8.rootfs.wic.bz2 | sudo dd of=/dev/sdX
```

## Debugging
Once booted you can connect to your device via ssh. Be careful default password is empty for root user!
Wifi can be configured as described [here](../aws-iot-greengrass-lite-demo-image/README.md#installation). That zip file can be downloaded from web console (AWS IoT -> AWS IoT Greengrass) when following the "Set up one Greengrass core device" wizard.

```bash
ssh root@<ThingName>
cat /etc/systemd/system/webrtc.service
```

## Testing
Use the official [Testpage](https://awslabs.github.io/amazon-kinesis-video-streams-webrtc-sdk-js/examples/index.html) and enter your keys and start as viewer. This should start a WebRTC connection with your device.
