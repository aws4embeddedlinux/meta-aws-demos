# Build AWS IoT Greengrass v2.0 on the NXP i.MX8MQEVK and i.MX8MPEVK

The following guide will build a Yocto image on an AWS instance for the NXP i.MX8MQuad EVK or i.MX8MPlus EVK that contains AWS IoT Greengrass v. 2.0 and the dependencies for Amazon SageMaker Edge Manager and the Deep Learning Runtime.

**Requirements** :
* An AWS account
* Host machine with a Unix terminal (Linux or Mac OS)
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) installed and configured on host machine
* i.MX8MQEVK or [MCIMX8M-EVK](https://www.nxp.com/design/development-boards/i-mx-evaluation-and-development-boards/evaluation-kit-for-the-i-mx-8m-applications-processor:MCIMX8M-EVK)

## 0. Create Cloud9 environment

Cloud9 is a cloud-managed IDE that we will use to build the Yocto image.

### 0.1 Create Cloud9 IDE

Navigate to the AWS Management Console, and then to Cloud 9. Click on 'Create environment' with the following parameters:
* Environment type: Create a new EC2 instance for environment (direct access)
* Instance type: ``c5.12xlarge``, with this instance type, a clean build should take approximately 35 minutes to build. 
* Platform: Ubuntu Server 18.04 LTS

c5.12xlarge is not available in all availability zones. If the creation fails, setup the environment again, and under 'Network settings (advanced)' change the Subnet to one of the recommended availability zones.

### 0.2 Add Additional Storage
Yocto builds require a large amount of storage and a high provisioned capacity for input/output of that storage device.

Navigate to the EC2 Management Console, and click on 'Instances'.

Look for your instance name that starts with 'aws-cloud9'. Click on it to open up the configuration pane on the bottom.
Choose 'Storage' and then click on the Volume ID of the root volume.

This will open up the Elastic Block Store console for that drive. Click on 'Actions' and 'Modify volume'. Change the volume to the following parameters:
* Volume Type: General Purpose SSD (gp3)
* Size: 500
* Iops: 10000
* Throughput: 500 MB/s

Modifying the volume can take up to 45 minutes. Monitor the status under the 'State' column on the EBS dashboard.

Next, we will extend the volume on the instance. Navigate to the Cloud9 Management Console, and choose 'Open IDE' next to your created Cloud9 environment. Open up a terminal in the IDE and run the following commands:

``lsblk``

You should see the following structure:
```
nvme0n1     259:0    0  500G  0 disk 
└─nvme0n1p1 259:1    0   10G  0 part /
```

Grow the partition:

```
sudo growpart /dev/nvme0n1 1
CHANGED: partition=1 start=2048 old: size=20969439 end=20971487 new: size=1048573919,end=1048575967
```
Ensure the partition is now being used fully:
```
lsblk
nvme0n1     259:0    0  500G  0 disk 
└─nvme0n1p1 259:1    0  500G  0 part /
```
Resize the filesystem on the partition:
```
sudo resize2fs /dev/nvme0n1p1
resize2fs 1.44.1 (24-Mar-2018)
Filesystem at /dev/nvme0n1p1 is mounted on /; on-line resizing required
old_desc_blocks = 2, new_desc_blocks = 63


The filesystem on /dev/nvme0n1p1 is now 131071739 (4k) blocks long.
```

## 1. Setup the build server / host environment.
Follow step 3.1 and 3.2 in the [i.MX Yocto Project User Guide](https://www.nxp.com/docs/en/user-guide/IMX_YOCTO_PROJECT_USERS_GUIDE.pdf) to install all of the necessary host dependencies on your Cloud9 instance.

## 2. Setup the i.MX Yocto project
Follow step 4 in the [i.MX Yocto Project User Guide](https://www.nxp.com/docs/en/user-guide/IMX_YOCTO_PROJECT_USERS_GUIDE.pdf)

## 3. Initialize the build environment
``cd imx-yocto-bsp``

To build the i.MX8MQEVK:
``DISTRO=fsl-imx-wayland MACHINE=imx8mqevk source imx-setup-release.sh -b build-dir``

To build the i.MX8MPEVK:
``DISTRO=fsl-imx-wayland MACHINE=imx8mpevk source imx-setup-release.sh -b build-dir``

## 4. Clone meta-aws
``cd imx-yocto-bsp/sources``

``git clone -b hardknott https://github.com/aws/meta-aws``

## 5. Setup your Yocto build
Open ``imx-yocto-bsp/build-dir/conf/local.conf`` and add the following lines:

Take advantage of processing power from your EC2 instance. PARALLEL_MAKE variable is passed as a direct argument to make. The [Yocto Project](https://www.yoctoproject.org/docs/1.4/ref-manual/ref-manual.html) recommends these variables be set to twice the number of cores on the build server. 
```
PARALLEL_MAKE = "-j 96"
BB_NUMBER_THREADS = "96"
```

Add the Greengrass recipe to your build. This will build Greengrass v2.

```
IMAGE_INSTALL_append = " greengrass-bin"
```

Add support for the AWS IoT Device Python SDK v2 to build Python applications that can interact with the Greengrass IPC:
```
IMAGE_INSTALL_append = " aws-iot-device-sdk-python-v2"
```

**OPTIONAL:** Add the following dependencies for Amazon SageMaker Neo Deep Learning Runtime and Amazon SageMaker Edge Manager:
```
IMAGE_INSTALL_append = " python3-grpcio-tools"
IMAGE_INSTALL_append = " python3-numpy"
IMAGE_INSTALL_append = " python3-grpcio"
IMAGE_INSTALL_append = " python3-protobuf"
IMAGE_INSTALL_append = " opencv"
```

local.conf should look similar to the following:
```
MACHINE ??= 'imx8mqevk'
DISTRO ?= 'fsl-imx-wayland'
PACKAGE_CLASSES ?= 'package_rpm'
EXTRA_IMAGE_FEATURES ?= "debug-tweaks"
USER_CLASSES ?= "buildstats image-mklibs image-prelink"
PATCHRESOLVE = "noop"
BB_DISKMON_DIRS ??= "\
    STOPTASKS,${TMPDIR},1G,100K \
    STOPTASKS,${DL_DIR},1G,100K \
    STOPTASKS,${SSTATE_DIR},1G,100K \
    STOPTASKS,/tmp,100M,100K \
    ABORT,${TMPDIR},100M,1K \
    ABORT,${DL_DIR},100M,1K \
    ABORT,${SSTATE_DIR},100M,1K \
    ABORT,/tmp,10M,1K"
PACKAGECONFIG_append_pn-qemu-system-native = " sdl"
CONF_VERSION = "1"

DL_DIR ?= "${BSPDIR}/downloads/"
ACCEPT_FSL_EULA = "1"

# Switch to Debian packaging and include package-management in the image
PACKAGE_CLASSES = "package_deb"
EXTRA_IMAGE_FEATURES += "package-management"

PARALLEL_MAKE= "-j 96"
BB_NUMBER_THREADS = "96"

IMAGE_INSTALL_append = " greengrass-bin"
IMAGE_INSTALL_append = " python3-grpcio-tools"
IMAGE_INSTALL_append = " python3-numpy"
IMAGE_INSTALL_append = " python3-grpcio"
IMAGE_INSTALL_append = " python3-protobuf"
IMAGE_INSTALL_append = " opencv"
IMAGE_INSTALL_append = " aws-iot-device-sdk-python-v2"

```

Add the meta-aws layer to `imx-yocto-bsp/build-dir/conf/bblayers.conf` under `BBLAYERS= "`:
  ```
  ${BSPDIR}/sources/meta-aws \
  ```

## 6. (OPTIONAL) Set Static IP Address
This step is optional, if you wish to have your i.MX8M board assigned a static IP address.

Create a file called ``static-ip.bbclass`` in the folder  ``imx-yocto-bsp/build-dir/classes`` and add the following lines to the file:

```
#This function creates and copies the /etc/systemd/network/80-wired.network file to the rootfs
#Takes precendence over /lib/systemd/network/80-wired.network
set_static_ip() {
cat <<EOF > 80-wired.network
[Match]
Name=eth0
[Network]
DHCP=no
Address=192.168.1.106/24
Gateway=192.168.1.1
EOF
cp 80-wired.network  ${IMAGE_ROOTFS}/etc/systemd/network 
}

ROOTFS_POSTINSTALL_COMMAND += " set_static_ip; "
```
Edit the IP addresses for Address and Gateway to your desired settings.

Add an additional line in ``imx-yocto-bsp/build-dir/conf/local.conf`` to ensure this is applied to your image at build time:
```
USER_CLASSES += "static-ip"
```

## 7. Build the image
``cd imx-yocto-bsp``

``bitbake imx-image-full``

Wait approximately 30-40 minutes for the build to complete (assuming you are working on a c5.12xlarge instance)

Subsequent builds for the same distro will take significantly less if the changes are incremental as long as you do not delete the build folder.

## 8. Download the image to a local machine
You can use S3 to upload the image from Cloud9 and distribute to any local machine.

[Create an S3 bucket](https://docs.aws.amazon.com/AmazonS3/latest/gsg/CreatingABucket.html) with default settings.

The SD card image name should be similar to the following: imx-image-full-imx8mqevk-20201223010742.gg.rootfs.wic.bz2, 'imx8mqevk' should be replaced with 'imx8mpevk' if you are using the i.MX8MPlus

``aws s3 cp imx-yocto-bsp/build-dir/tmp/deploy/images/imx8mqebvk/imx-image-full-imx8mqevk-20201223010742.gg.rootfs.wic.bz2 s3://your-bucket-name``

Pre sign the image.

``aws s3 presign s3://your-bucket-name/imx-image-full-imx8mqevk-20201223010742.gg.rootfs.wic.bz2 --expires-in 604800``

The URL that is an output of this command will download the image to your local machine.

## 9. Prepare an SD card.
You will need an SD card with at least 8GB space. The approximate image size uncompressed is 6.29 GB.

Insert the SD card to your local machine and determine onto which /dev/ it's been assigned. 

``diskutil list``

Format the disk and remove any partitions already present.

``sudo diskutil eraseDisk free itsfree /dev/<your disk>``

## 10. Flash the image to an SD card

Decompress the image:

``bunzip2 -dk -f <image_name>.wic.bz2``

Flash the SD card. This process takes approximately 15 minutes:

``sudo dd if=<image name>.wic of=/dev/<your disk> bs=1m && sync``

Unmount the SD card before proceeding:

``sudo diskutil unmountDisk <your disk>``

## 11. Prepare the device

### i.MX8MQEVK
Set SW801 dip switches to boot from an SD card. 1&2 should be high, 3&4 should be low.
Insert the SD card, an Ethernet cable, and power on the device.

### i.MX8MPEVK
Set SW4 boot dip switches to boot from an SD card. 1&2 shoud be low, 3&4 should be high.
Insert the SD card, an Ethernet cable, and power on the device.

## 12. Access the device over SSH
Wait for the device to power on. It is configured for DHCP on the Ethernet port.

``ssh root@<IP address>``

## 13. Check the Greengrass installation
``cd /greengrass/v2``

## 14. Create AWS IoT Resources
**To create an AWS IoT thing**

1. Create an AWS IoT thing for your device\. On your development computer, run the following command\.
   + Replace *MyGreengrassCore* with the thing name to use\. This name is also the name of your Greengrass core device\.
**Note**  <a name="install-argument-thing-name-constraint"></a>
The thing name can't contain colon \(`:`\) characters\.

   ```
   aws iot create-thing --thing-name MyGreengrassCore
   ```

   The response looks similar to the following example, if the request succeeds\.

   ```
   {
     "thingName": "MyGreengrassCore",
     "thingArn": "arn:aws:iot:us-west-2:123456789012:thing/MyGreengrassCore",
     "thingId": "8cb4b6cd-268e-495d-b5b9-1713d71dbf42"
   }
   ```

1. Create a folder where you download the certificates for the AWS IoT thing\.

   ```
   mkdir greengrass-v2-certs
   ```

1. Create and download the certificates for the AWS IoT thing\.

   ```
   aws iot create-keys-and-certificate --set-as-active --certificate-pem-outfile greengrass-v2-certs/device.pem.crt --public-key-outfile greengrass-v2-certs/public.pem.key --private-key-outfile greengrass-v2-certs/private.pem.key
   ```

   The response looks similar to the following example, if the request succeeds\.

   ```
   {
     "certificateArn": "arn:aws:iot:us-west-2:123456789012:cert/aa0b7958770878eabe251d8a7ddd547f4889c524c9b574ab9fbf65f32248b1d4",
     "certificateId": "aa0b7958770878eabe251d8a7ddd547f4889c524c9b574ab9fbf65f32248b1d4",
     "certificatePem": "-----BEGIN CERTIFICATE-----
   MIICiTCCAfICCQD6m7oRw0uXOjANBgkqhkiG9w
    0BAQUFADCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAldBMRAwDgYDVQQHEwdTZ
    WF0dGxlMQ8wDQYDVQQKEwZBbWF6b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIw
    EAYDVQQDEwlUZXN0Q2lsYWMxHzAdBgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5
    jb20wHhcNMTEwNDI1MjA0NTIxWhcNMTIwNDI0MjA0NTIxWjCBiDELMAkGA1UEBh
    MCVVMxCzAJBgNVBAgTAldBMRAwDgYDVQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBb
    WF6b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMx
    HzAdBgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5jb20wgZ8wDQYJKoZIhvcNAQE
    BBQADgY0AMIGJAoGBAMaK0dn+a4GmWIWJ21uUSfwfEvySWtC2XADZ4nB+BLYgVI
    k60CpiwsZ3G93vUEIO3IyNoH/f0wYK8m9TrDHudUZg3qX4waLG5M43q7Wgc/MbQ
    ITxOUSQv7c7ugFFDzQGBzZswY6786m86gpEIbb3OhjZnzcvQAaRHhdlQWIMm2nr
    AgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAtCu4nUhVVxYUntneD9+h8Mg9q6q+auN
    KyExzyLwaxlAoo7TJHidbtS4J5iNmZgXL0FkbFFBjvSfpJIlJ00zbhNYS5f6Guo
    EDmFJl0ZxBHjJnyp378OD8uTs7fLvjx79LjSTbNYiytVbZPQUQ5Yaxu2jXnimvw
    3rrszlaEXAMPLE=
   -----END CERTIFICATE-----",
     "keyPair": {
       "PublicKey": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkEXAMPLEQEFAAOCAQ8AMIIBCgKCAQEAEXAMPLE1nnyJwKSMHw4h\nMMEXAMPLEuuN/dMAS3fyce8DW/4+EXAMPLEyjmoF/YVF/gHr99VEEXAMPLE5VF13\n59VK7cEXAMPLE67GK+y+jikqXOgHh/xJTwo+sGpWEXAMPLEDz18xOd2ka4tCzuWEXAMPLEahJbYkCPUBSU8opVkR7qkEXAMPLE1DR6sx2HocliOOLtu6Fkw91swQWEXAMPLE\GB3ZPrNh0PzQYvjUStZeccyNCx2EXAMPLEvp9mQOUXP6plfgxwKRX2fEXAMPLEDa\nhJLXkX3rHU2xbxJSq7D+XEXAMPLEcw+LyFhI5mgFRl88eGdsAEXAMPLElnI9EesG\nFQIDAQAB\n-----END PUBLIC KEY-----\n",
       "PrivateKey": "-----BEGIN RSA PRIVATE KEY-----\nkey omitted for security reasons\n-----END RSA PRIVATE KEY-----\n"
     }
   }
   ```

1. Attach the certificate to the AWS IoT thing\.
   + Replace *MyGreengrassCore* with the name of your AWS IoT thing\.
   + Replace the certificate Amazon Resource Name \(ARN\) with the ARN of the certificate that you created in the previous step\.

   ```
   aws iot attach-thing-principal --thing-name MyGreengrassCore --principal arn:aws:iot:us-west-2:123456789012:cert/aa0b7958770878eabe251d8a7ddd547f4889c524c9b574ab9fbf65f32248b1d4
   ```

   The command doesn't have any output if the request succeeds\.

1. Create and attach an AWS IoT policy that defines the AWS IoT permissions for your Greengrass core device\. The following policy allows access to all MQTT topics and Greengrass operations, so your device works with custom applications and future changes that require new Greengrass operations\. You can restrict this policy down based on your use case\. For more information, see [Minimal AWS IoT policy for AWS IoT Greengrass V2 core devices](device-auth.md#greengrass-core-minimal-iot-policy)\.

   If you have set up a Greengrass core device before, you can attach its AWS IoT policy instead of creating a new one\.

   Do the following:

   1. \(Optional\) Create a file that contains the AWS IoT policy document that Greengrass core devices require\.

      ```
      nano greengrass-v2-iot-policy.json
      ```

      Copy the following JSON into the file\.

      ```
      {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "iot:Publish",
              "iot:Subscribe",
              "iot:Receive",
              "iot:Connect",
              "greengrass:*"
            ],
            "Resource": [
              "*"
            ]
          }
        ]
      }
      ```

   1. \(Optional\) Create an AWS IoT policy from the policy document\.
      + Replace *GreengrassV2IoTThingPolicy* with the name of the policy to create\.

      ```
      aws iot create-policy --policy-name GreengrassV2IoTThingPolicy --policy-document file://greengrass-v2-iot-policy.json
      ```

      The response looks similar to the following example, if the request succeeds\.

      ```
      {
        "policyName": "GreengrassV2IoTThingPolicy",
        "policyArn": "arn:aws:iot:us-west-2:123456789012:policy/GreengrassV2IoTThingPolicy",
        "policyDocument": "{
          \"Version\": \"2012-10-17\",
          \"Statement\": [
            {
              \"Effect\": \"Allow\",
              \"Action\": [
                \"iot:Publish\",
                \"iot:Subscribe\",
                \"iot:Receive\",
                \"iot:Connect\",
                \"greengrass:*\"
              ],
              \"Resource\": [
                \"*\"
              ]
            }
          ]
        }",
        "policyVersionId": "1"
      }
      ```

   1. Attach the AWS IoT policy to the AWS IoT thing's certificate\.
      + Replace *GreengrassV2IoTThingPolicy* with the name of the policy to attach\.
      + Replace the target ARN with the ARN of the certificate for your AWS IoT thing\.

      ```
      aws iot attach-policy --policy-name GreengrassV2IoTThingPolicy --target arn:aws:iot:us-west-2:123456789012:cert/aa0b7958770878eabe251d8a7ddd547f4889c524c9b574ab9fbf65f32248b1d4
      ```

      The command doesn't have any output if the request succeeds\.

1. \(Optional\) Add the AWS IoT thing to a new or existing thing group\. You use thing groups to manage fleets of Greengrass core devices\. When you deploy software components to your devices, you can choose to target individual devices or groups of devices\. You can add a device to a thing group with an active Greengrass deployment to deploy that thing group's software components to the device\. Do the following:

   1. \(Optional\) Create an AWS IoT thing group\.
      + Replace *MyGreengrassCoreGroup* with the name of the thing group to create\.
**Note**  <a name="install-argument-thing-group-name-constraint"></a>
The thing group name can't contain colon \(`:`\) characters\.

      ```
      aws iot create-thing-group --thing-group-name MyGreengrassCoreGroup
      ```

      The response looks similar to the following example, if the request succeeds\.

      ```
      {
        "thingGroupName": "MyGreengrassCoreGroup",
        "thingGroupArn": "arn:aws:iot:us-west-2:123456789012:thinggroup/MyGreengrassCoreGroup",
        "thingGroupId": "4df721e1-ff9f-4f97-92dd-02db4e3f03aa"
      }
      ```

   1. Add the AWS IoT thing to a thing group\.
      + Replace *MyGreengrassCore* with the name of your AWS IoT thing\.
      + Replace *MyGreengrassCoreGroup* with the name of the thing group\.

      ```
      aws iot add-thing-to-thing-group --thing-name MyGreengrassCore --thing-group-name MyGreengrassCoreGroup
      ```

      The command doesn't have any output if the request succeeds\.

### Retrieve AWS IoT endpoints<a name="retrieve-iot-endpoints"></a>

Get the AWS IoT endpoints for your AWS account, and save them to use later\. Your device uses these endpoints to connect to AWS IoT\. Do the following:

1. Get the AWS IoT data endpoint for your AWS account\.

   ```
   aws iot describe-endpoint --endpoint-type iot:Data-ATS
   ```

   The response looks similar to the following example, if the request succeeds\.

   ```
   {
     "endpointAddress": "device-data-prefix-ats.iot.us-west-2.amazonaws.com"
   }
   ```

1. Get the AWS IoT credentials endpoint for your AWS account\.

   ```
   aws iot describe-endpoint --endpoint-type iot:CredentialProvider
   ```

   The response looks similar to the following example, if the request succeeds\.

   ```
   {
     "endpointAddress": "device-credentials-prefix.credentials.iot.us-west-2.amazonaws.com"
   }
   ```

### Create a token exchange role<a name="create-token-exchange-role"></a>

Greengrass core devices use an IAM service role, called the *token exchange role*, to authorize calls to AWS services\. The device uses the AWS IoT credentials provider to get temporary AWS credentials for this role, which allows the device to interact with AWS IoT, send logs to Amazon CloudWatch Logs, and download custom component artifacts from Amazon S3\. For more information, see [Authorize core devices to interact with AWS services](device-service-role.md)\.

You use an AWS IoT *role alias* to configure the token exchange role for a Greengrass core device\. Role aliases enable you to change the token exchange role for a device but keep the device configuration the same\. For more information, see [Authorizing direct calls to AWS services](https://docs.aws.amazon.com/iot/latest/developerguide/authorizing-direct-aws.html) in the *AWS IoT Core Developer Guide*\.

In this section, you create a token exchange IAM role and an AWS IoT role alias that points to the role\. If you have already set up a Greengrass core device, you can use its token exchange role and role alias instead of creating new ones\. Then, you configure your device's AWS IoT thing to use that role and alias\.

**To create a token exchange IAM role**

1. \(Optional\) Create an IAM role that your device can use as a token exchange role\. Do the following:

   1. Create a file that contains the trust policy document that the token exchange role requires\.

      ```
      nano device-role-trust-policy.json
      ```

      Copy the following JSON into the file\.

      ```
      {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Principal": {
              "Service": "credentials.iot.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
          }
        ]
      }
      ```

   1. Create the token exchange role with the trust policy document\.
      + Replace *GreengrassV2TokenExchangeRole* with the name of the IAM role to create\.

      ```
      aws iam create-role --role-name GreengrassV2TokenExchangeRole --assume-role-policy-document file://device-role-trust-policy.json
      ```

      The response looks similar to the following example, if the request succeeds\.

      ```
      {
        "Role": {
          "Path": "/",
          "RoleName": "GreengrassV2TokenExchangeRole",
          "RoleId": "AROAZ2YMUHYHK5OKM77FB",
          "Arn": "arn:aws:iam::123456789012:role/GreengrassV2TokenExchangeRole",
          "CreateDate": "2021-02-06T00:13:29+00:00",
          "AssumeRolePolicyDocument": {
            "Version": "2012-10-17",
            "Statement": [
              {
                "Effect": "Allow",
                "Principal": {
                  "Service": "credentials.iot.amazonaws.com"
                },
                "Action": "sts:AssumeRole"
              }
            ]
          }
        }
      }
      ```

   1. Create a file that contains the access policy document that the token exchange role requires\.

      ```
      nano device-role-access-policy.json
      ```

      Copy the following JSON into the file\.

      ```
      {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "iot:DescribeCertificate",
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents",
              "logs:DescribeLogStreams",
              "iot:Connect",
              "iot:Publish",
              "iot:Subscribe",
              "iot:Receive",
              "s3:GetBucketLocation"
            ],
            "Resource": "*"
          }
        ]
      }
      ```
**Note**  
This access policy doesn't allow access to component artifacts in S3 buckets\. To deploy custom components that define artifacts in Amazon S3, you must add permissions to the role to allow your core device to retrieve component artifacts\. For more information, see [Allow access to S3 buckets for component artifacts](device-service-role.md#device-service-role-access-s3-bucket)\.  
If you don't yet have an S3 bucket for component artifacts, you can add these permissions later after you create a bucket\.

   1. Create the IAM policy from the policy document\.
      + Replace *GreengrassV2TokenExchangeRoleAccess* with the name of the IAM policy to create\.

      ```
      aws iam create-policy --policy-name GreengrassV2TokenExchangeRoleAccess --policy-document file://device-role-access-policy.json
      ```

      The response looks similar to the following example, if the request succeeds\.

      ```
      {
        "Policy": {
          "PolicyName": "GreengrassV2TokenExchangeRoleAccess",
          "PolicyId": "ANPAZ2YMUHYHACI7C5Z66",
          "Arn": "arn:aws:iam::123456789012:policy/GreengrassV2TokenExchangeRoleAccess",
          "Path": "/",
          "DefaultVersionId": "v1",
          "AttachmentCount": 0,
          "PermissionsBoundaryUsageCount": 0,
          "IsAttachable": true,
          "CreateDate": "2021-02-06T00:37:17+00:00",
          "UpdateDate": "2021-02-06T00:37:17+00:00"
        }
      }
      ```

   1. Attach the IAM policy to the token exchange role\.
      + Replace *GreengrassV2TokenExchangeRole* with the name of the IAM role\.
      + Replace the policy ARN with the ARN of the IAM policy that you created in the previous step\.

      ```
      aws iam attach-role-policy --role-name GreengrassV2TokenExchangeRole --policy-arn arn:aws:iam::123456789012:policy/GreengrassV2TokenExchangeRoleAccess
      ```

      The command doesn't have any output if the request succeeds\.

1. \(Optional\) Create an AWS IoT role alias that points to the token exchange role\.
   + Replace *GreengrassCoreTokenExchangeRoleAlias* with the name of the role alias to create\.
   + Replace the role ARN with the ARN of the IAM role that you created in the previous step\.

   ```
   aws iot create-role-alias --role-alias GreengrassCoreTokenExchangeRoleAlias --role-arn arn:aws:iam::123456789012:role/GreengrassV2TokenExchangeRole
   ```

   The response looks similar to the following example, if the request succeeds\.

   ```
   {
     "roleAlias": "GreengrassCoreTokenExchangeRoleAlias",
     "roleAliasArn": "arn:aws:iot:us-west-2:123456789012:rolealias/GreengrassCoreTokenExchangeRoleAlias"
   }
   ```
**Note**  
To create a role alias, you must have permission to pass the token exchange IAM role to AWS IoT\. If you receive an error when you try to create a role alias, check that your AWS user has this permission\. For more information, see [Granting a user permissions to pass a role to an AWS service](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_use_passrole.html) in the *AWS Identity and Access Management User Guide*\.

1. Create and attach an AWS IoT policy that allows your Greengrass core device to use the role alias to assume the token exchange role\. If you have set up a Greengrass core device before, you can attach its role alias AWS IoT policy instead of creating a new one\. Do the following:

   1. \(Optional\) Create a file that contains the AWS IoT policy document that the role alias requires\.

      ```
      nano greengrass-v2-iot-role-alias-policy.json
      ```

      Copy the following JSON into the file\.
      + Replace the resource ARN with the ARN of your role alias\.

      ```
      {
        "Version":"2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": "iot:AssumeRoleWithCertificate",
            "Resource": "arn:aws:iot:us-west-2:123456789012:rolealias/GreengrassCoreTokenExchangeRoleAlias"
          }
        ]
      }
      ```

   1. \(Optional\) Create an AWS IoT policy from the policy document\.
      + Replace *GreengrassCoreTokenExchangeRoleAliasPolicy* with the name of the AWS IoT policy to create\.

      ```
      aws iot create-policy --policy-name GreengrassCoreTokenExchangeRoleAliasPolicy --policy-document file://greengrass-v2-iot-role-alias-policy.json
      ```

      The response looks similar to the following example, if the request succeeds\.

      ```
      {
        "policyName": "GreengrassCoreTokenExchangeRoleAliasPolicy",
        "policyArn": "arn:aws:iot:us-west-2:123456789012:policy/GreengrassCoreTokenExchangeRoleAliasPolicy",
        "policyDocument": "{
          \"Version\":\"2012-10-17\",
          \"Statement\": [
            {
              \"Effect\": \"Allow\",
              \"Action\": \"iot:AssumeRoleWithCertificate\",
              \"Resource\": \"arn:aws:iot:us-west-2:123456789012:rolealias/GreengrassCoreTokenExchangeRoleAlias\"
            }
          ]
        }",
        "policyVersionId": "1"
      }
      ```

   1. Attach the AWS IoT policy to the AWS IoT thing's certificate\.
      + Replace *GreengrassCoreTokenExchangeRoleAliasPolicy* with the name of the role alias AWS IoT policy\.
      + Replace the target ARN with the ARN of the certificate for your AWS IoT thing\.

      ```
      aws iot attach-policy --policy-name GreengrassCoreTokenExchangeRoleAliasPolicy --target arn:aws:iot:us-west-2:123456789012:cert/aa0b7958770878eabe251d8a7ddd547f4889c524c9b574ab9fbf65f32248b1d4
      ```

      The command doesn't have any output if the request succeeds\.

### Download certificates to the device<a name="download-thing-certificates"></a>

Earlier, you downloaded your device's certificates to your development computer\. In this section, you copy these certificates to your device set up the device with the certificates that it uses to connect to AWS IoT\.

**To download certificates to the device**

1. Copy the AWS IoT thing certificates from your development computer to the device\. You might be able to use the `scp` command, for example\.
   + Replace *device\-ip\-address* with the IP of your device\.

   ```
   scp -r greengrass-v2-certs/ device-ip-address:~
   ```

1. On the device, download the Amazon root certificate authority \(CA\) certificate\. AWS IoT certificates are associated with Amazon's root CA certificate by default\.

   ```
   wget https://www.amazontrust.com/repository/AmazonRootCA1.pem
   ```

1. Copy the AWS IoT thing certificates to the Greengrass root folder\.
   + Replace */greengrass/v2* with the Greengrass root folder\.

   ```
   sudo cp -R ~/greengrass-v2-certs/* /greengrass/v2
   ```
## 15. Configure Greengrass

In this step we will edit the Greengrass configuration to use the correct AWS IoT Core endpoint and certificates.

First, remove the default configurations:
   ```
   rm /greengrass/v2/config/*
   ```
   
Next, create a new configuration:

   ```
   nano /greengrass/v2/config/config.yaml
   ```

   Copy the following YAML content into the file\. This partial configuration file specifies system parameters and Greengrass nucleus parameters\.

   ```
   ---
   system:
     certificateFilePath: "/greengrass/v2/device.pem.crt"
     privateKeyPath: "/greengrass/v2/private.pem.key"
     rootCaPath: "/greengrass/v2/AmazonRootCA1.pem"
     rootpath: "/greengrass/v2"
     thingName: "MyGreengrassCore"
   services:
     aws.greengrass.Nucleus:
       componentType: "NUCLEUS"
       version: "2.1.0"
       configuration:
         awsRegion: "us-west-2"
         iotRoleAlias: "GreengrassCoreTokenExchangeRoleAlias"
         iotDataEndpoint: "device-data-prefix-ats.iot.us-west-2.amazonaws.com"
         iotCredEndpoint: "device-credentials-prefix.credentials.iot.us-west-2.amazonaws.com"
   ```

   Then, do the following:
   + Replace each instance of */greengrass/v2* with the Greengrass root folder\.
   + Replace *MyGreengrassCore* with the name of the AWS IoT thing\.
   + Replace *2\.1\.0* with the version of the AWS IoT Greengrass Core software\.
   + Replace *us\-west\-2* with the AWS Region where you created the resources\.
   + Replace *GreengrassCoreTokenExchangeRoleAlias* with the token exchange role alias\.
   + Replace the `iotDataEndpoint` with your AWS IoT data endpoint\.
   + Replace the `iotCredEndpoint` with your AWS IoT credentials endpoint\.

**Note**  
In this configuration file, you can customize other nucleus configuration options such as the ports and network proxy to use, as shown in the following example\. For more information, see [Greengrass nucleus configuration](greengrass-nucleus-component.md#greengrass-nucleus-component-configuration)\.  

## 16. Start Greengrass
Greengrass should already be running using the systemctl service. After configuring Greengrass with the correct connection information, restart Greengrass:

```
systemctl restart greengrass
```

You can check the status of your Greengrass core connection by viewing the Greengrass logs:
```
tail -f /greengrass/v2/logs/greengrass.log
```

If your setup has been successful, you will be able to view the device in the AWS IoT Management Console.
1. Open up the AWS IoT Core Management Console
2. Click on 'Greengrass' and then 'Core Devices'
3. Look for your Thing Name and under Status the device should report as 'HEALTHY' 

## 17. Next Steps
Now that your NXP i.MX8M has AWS IoT Greengrass v2 installed and it is connected to AWS IoT Core, you can use the Greengrass deployment mechanisms to install software over the air. If you want to explore Greengrass v2 and edge machine learning with Amazon SageMaker, please see [Greengrass v2 and Amazon SageMaker Edge Manager workshop](https://github.com/dhwalters423/greengrass-v2-sagemaker-edge-manager-python).
