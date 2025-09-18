# aws-iot-greengrass-lite-demo-simple-image-tpm

This image is similar to [aws-iot-greengrass-lite-demo-simple-image](../aws-iot-greengrass-lite-demo-simple-image/README.md)

The main difference it is supporting a TPM module.

Tested with `aws-ec2-x86-64` and `raspberrypi4-tpm`



Note: for aws-ec2-x86-64 you need to upload the snapshot with tpm support and start with a nitro instance that suppors tpm (e.g. m5n.large).


Modify / Create a new ami after upload
```
aws ec2 register-image \
    --name my-tpm-image \
    --boot-mode uefi \
    --architecture x86_64 \
    --root-device-name /dev/sda1 \
    --block-device-mappings DeviceName=/dev/sda1,Ebs={SnapshotId=snap-xxxxxxxxxxxxxx} \
    --tpm-support v2.0
```


More information here: https://github.com/aws-greengrass/aws-greengrass-lite/blob/main/docs/TPM_SUPPORT.md


Add the following configuration here: /etc/ssl/openssl.cnf

```bash
[openssl_init]
providers = provider_sect

[provider_sect]
default = default_sect
tpm2 = tpm2_sect

[default_sect]
activate = 1

[tpm2_sect]
identity = tpm2
module = /usr/lib/ossl-modules/tpm2.so
activate = 1
```


example config.yaml
```
---
system:
  rootPath: "/var/lib/greengrass"
  privateKeyPath: "handle:0x81000002"
  certificateFilePath: "/etc/greengrass/ggcredentials/device.pem"
  rootCaPath: "/etc/greengrass/ggcredentials/AmazonRootCA1.pem"
  rootPath: "/var/lib/greengrass"
  thingName: "TPMThing"
services:
  aws.greengrass.NucleusLite:
    componentType: "NUCLEUS"
    configuration:
      runWithDefault:
        posixUser: "ggcore:ggcore"
      greengrassDataPlanePort: "8443"
      platformOverride: {}
      awsRegion: "eu-central-1"
      iotRoleAlias: "GreengrassV2TokenExchangeCoreDeviceRoleAlias"
      iotDataEndpoint: "a20mxm1jboggkj-ats.iot.eu-central-1.amazonaws.com"
      iotCredEndpoint: "c2cw693ei5usp5.credentials.iot.eu-central-1.amazonaws.com"
```








Sep 18 14:40:21 raspberrypi4-64 iotcored[667]: E[iotcored] tls.c:255: Failed TLS handshake.
Sep 18 14:40:21 raspberrypi4-64 iotcored[667]: E[iotcored] tls.c:50: [openssl]: A0F15DB47F000000:error:0800007D:elliptic curve routines:ossl_ecdsa_simple_sign_sig:missing private key:/usr/src/debug/openssl/3.2.4/crypto/ec/ecdsa_ossl.c:291:
Sep 18 14:40:21 raspberrypi4-64 iotcored[667]: E[iotcored] tls.c:50: [openssl]: A0F15DB47F000000:error:0A080006:SSL routines:tls_construct_cert_verify:EVP lib:/usr/src/debug/openssl/3.2.4/ssl/statem/statem_lib.c:406:
Sep 18 14:40:21 raspberrypi4-64 iotcored[667]: E[iotcored] mqtt.c:284: Failed to create TLS connection.
