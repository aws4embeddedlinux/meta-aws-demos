# aws-webrtc-demo-image
A image to test [amazon-kvs-producer-sdk-c](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c).

Tested on device: [qemu-arm64](../../../conf/devices/qemu-arm64/README.md)

Setup [environment (AWS_ACCESS_KEY_ID + AWS_SECRET_ACCESS_KEY)](https://github.com/awslabs/amazon-kinesis-video-streams-webrtc-sdk-c?tab=readme-ov-file#run)

Run this to send audio-video to Kinesis Video Streams e.g.
```bash
kvsWebrtcClientMasterGstSample test audio-video testsrc
```
