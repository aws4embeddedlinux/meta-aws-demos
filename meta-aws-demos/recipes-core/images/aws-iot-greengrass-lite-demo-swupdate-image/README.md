# aws-iot-greengrass-lite-demo-swupdate-image
Similar to
But demo for (swupdate[])https://sbabic.github.io/swupdate/index.html]

Tested with: `raspberrypi-64`

```bash
export MACHINE=raspberrypi-64
export IMAGE=aws-iot-greengrass-lite-demo-swupdate-image

bitbake aws-iot-greengrass-lite-demo-swupdate-image
bitbake aws-iot-greengrass-lite-demo-swupdate-file
```

Flash `aws-iot-greengrass-lite-demo-swupdate-image-raspberrypi-armv8.rootfs.wic.bz2` onto the sdcard.
(`sudo bzcat aws-iot-greengrass-lite-demo-swupdate-image-raspberrypi-armv8.rootfs.wic.bz2 | sudo dcfldd of=/dev/sdXXX`)

Use `aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu` as an update file for sw-update

Eg.
```bash
scp aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu root@192.168.0.192:/tmp/

# to install on rootfs_A
swupdate  -i /tmp/aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu  -H raspberrypi-armv8:1.0 -e stable,copy2

# to install on rootfs_B
swupdate  -i /tmp/aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu  -H raspberrypi-armv8:1.0 -e stable,copy1

```


## How to use in a gg component

The component operates in two main phases:

1. **Bootstrap**: Installs the RAUC bundle update.
2. **Startup**: Verifies the installation by comparing the hash of the installed bundle with the currently running slot.

## Configuration

The update file (`aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu`) is stored in an S3 bucket. Ensure the S3 URI in the component recipe is updated to point to your specific update file location.
Do modify the bucket name, version etc.

```yaml
---
RecipeFormatVersion: '2020-01-25'
ComponentName: 'com.example.AbUpdateSwUpdate'
ComponentVersion: '1.0.1'
ComponentDescription: 'Manages A/B system updates using swupdate'
ComponentPublisher: 'Example Corp'
ComponentType: 'aws.greengrass.generic'
ComponentDependencies:
  aws.greengrass.TokenExchangeService:
    VersionRequirement: ">=2.0.0"
    DependencyType: HARD
Manifests:
  - Platform:
      os: 'linux'
      runtime: "*"
    Lifecycle:
      bootstrap:
        Script: |
          echo Bootstrap
          rootfs=`swupdate -g`
          if [ $rootfs == '/dev/mmcblk0p2' ];then
            selection="-e stable,copy2"
          else
            selection="-e stable,copy1"
          fi
          sudo swupdate -i {artifacts:path}/aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu -H raspberrypi-armv8:1.0 ${selection}
        RequiresPrivilege: true
      startup:
        Script: |
          echo Startup
          rauc status
          current_booted_slot_bundle_hash=$(rauc status --detailed --output-format=json-pretty | jq -r '.slots[] | select(.[].state == "booted") | .[].slot_status.bundle.hash')
          bundle_hash=$(rauc info --output-format=json-pretty {artifacts:path}/aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu | jq -r '.hash')
          if [ "$current_booted_slot_bundle_hash" == "$bundle_hash" ]; then
              echo "Bundle image hash matches the current running slot"
          else
              echo "Bundle image hash differs from the current running slot"
              exit 1
          fi
    Artifacts:
      - URI: 's3://2024-11-27-us-east-1ab-update/aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu'
        Unarchive: 'NONE'
```


## Configuration for streaming updates

The update file (`aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu`) is stored in an S3 bucket. Ensure the S3 URI in the component recipe is updated to point to your specific update file location.
Do modify the bucket name, version etc.

```yaml
---
RecipeFormatVersion: '2020-01-25'
ComponentName: 'com.example.AbUpdateSwUpdateStreaming'
ComponentVersion: '1.0.1'
ComponentDescription: 'Manages A/B system updates using swupdate streaming'
ComponentPublisher: 'Example Corp'
ComponentType: 'aws.greengrass.generic'
Manifests:
  - Platform:
      os: 'linux'
      runtime: "*"
    Lifecycle:
      bootstrap:
        Script: |
          echo Bootstrap
          BUCKET=swupdate-yocto-test-bucket
          UPDATEFILE=aws-iot-greengrass-lite-demo-swupdate-file-raspberrypi-armv8.rootfs.swu
          REGION=$(aws s3api get-bucket-location --bucket "$BUCKET" --query LocationConstraint --output text)
          echo $REGION
          BUNDLE_URL=$(aws s3 presign "s3://$BUCKET/$UPDATEFILE" --expires-in 3600 --endpoint-url "https://s3.$REGION.amazonaws.com")
          echo $BUNDLE_URL
          rootfs=`swupdate -g`
          if [ $rootfs == '/dev/mmcblk0p2' ];then
            selection="-e stable,copy2"
          else
            selection="-e stable,copy1"
          fi
          sudo swupdate -d -u"$BUNDLE_URL" -H raspberrypi-armv8:1.0 ${selection}
        RequiresPrivilege: true
      startup:
        Script: |
          echo Startup
```
