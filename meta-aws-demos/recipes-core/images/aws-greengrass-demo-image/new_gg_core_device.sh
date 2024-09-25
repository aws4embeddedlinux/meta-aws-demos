#!/bin/bash

set -euxo pipefail
NEW_GG_CORE_DEVICE=$(date "+%F_%H-%M-%S")

AWS_ACCOUNT_NUMBER=$(aws sts get-caller-identity --query "Account" --output text)

AWS_REGION=$(aws ec2 describe-availability-zones --output text --query 'AvailabilityZones[0].[RegionName]')

aws iot create-thing --thing-name $NEW_GG_CORE_DEVICE

mkdir $NEW_GG_CORE_DEVICE

aws iot create-keys-and-certificate --set-as-active --certificate-pem-outfile $NEW_GG_CORE_DEVICE/device.pem.crt --public-key-outfile $NEW_GG_CORE_DEVICE/public.pem.key --private-key-outfile $NEW_GG_CORE_DEVICE/private.pem.key >> $NEW_GG_CORE_DEVICE/create-keys-and-certificate.json 

aws iot attach-thing-principal --thing-name $NEW_GG_CORE_DEVICE --principal `jq -r '.certificateArn' $NEW_GG_CORE_DEVICE/create-keys-and-certificate.json`

# Create a IAM policy
GREENGRASS_V2_IOT_ROLE_NAME=GreengrassV2IoTThingPolicy
GREENGRASS_V2_IOT_POLICY_DOCUMENT='{
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
}'

# Attempt to create the IOT role
set +euxo pipefail
create_role_output=$(aws iot create-policy --policy-name "$GREENGRASS_V2_IOT_ROLE_NAME" --policy-document "$GREENGRASS_V2_IOT_POLICY_DOCUMENT" 2>&1)
set -euxo pipefail

# Check if the role already exists
if echo "$create_role_output" | grep -q 'ResourceAlreadyExistsException'; then
  echo "IOT role already exists, skipping creation."
else
  echo "$create_role_output"
fi

# Attach the AWS IoT policy to the AWS IoT thing's certificate.
aws iot attach-policy --policy-name "$GREENGRASS_V2_IOT_ROLE_NAME" --target `jq -r '.certificateArn' $NEW_GG_CORE_DEVICE/create-keys-and-certificate.json`

# Create a token exchange role
GREENGRASS_V2_TOKEN_EXCHANGE_ROLE_NAME=GreengrassV2TokenExchangeRole
DEVICE_ROLE_TRUST_POLICY_DOCUMENT='{
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
}'

# Attempt to create token exchange role
set +euxo pipefail
create_role_output=$(aws iam create-role --role-name "$GREENGRASS_V2_TOKEN_EXCHANGE_ROLE_NAME" --assume-role-policy-document "$DEVICE_ROLE_TRUST_POLICY_DOCUMENT" 2>&1)
set -euxo pipefail

# Check if the role already exists
if echo "$create_role_output" | grep -q 'EntityAlreadyExists'; then
  echo "token exchange role already exists, skipping creation."
else
  echo "$create_role_output"
fi



# Create a IAM policy
DEVICE_ROLE_ACCESS_POLICY_NAME=GreengrassV2TokenExchangeRoleAccess
DEVICE_ROLE_ACCESS_POLICY_DOCUMENT='{
"Version": "2012-10-17",
  "Statement": [
    {
"Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams",
        "s3:GetBucketLocation"
      ],
      "Resource": "*"
    }
  ]
}'

# Attempt to create token exchange role access
set +euxo pipefail
create_role_output=$(aws iam create-policy --policy-name "$DEVICE_ROLE_ACCESS_POLICY_NAME" --policy-document "$DEVICE_ROLE_ACCESS_POLICY_DOCUMENT" 2>&1)
set -euxo pipefail

# Check if the role already exists
if echo "$create_role_output" | grep -q 'EntityAlreadyExists'; then
  echo policy "$DEVICE_ROLE_ACCESS_POLICY_NAME" already exists, skipping creation."
else
  echo "$create_role_output"
fi


# Attach the IAM policy to the token exchange role.
aws iam attach-role-policy --role-name "$GREENGRASS_V2_TOKEN_EXCHANGE_ROLE_NAME" --policy-arn arn:aws:iam::$AWS_ACCOUNT_NUMBER:policy/$DEVICE_ROLE_ACCESS_POLICY_NAME

# Create an AWS IoT role alias that points to the token exchange role.
set +euxo pipefail
create_role_output=$(aws iot create-role-alias --role-alias "$GREENGRASS_V2_TOKEN_EXCHANGE_ROLE_NAME" --role-arn arn:aws:iam::$AWS_ACCOUNT_NUMBER:role/$GREENGRASS_V2_TOKEN_EXCHANGE_ROLE_NAME 2>&1)
set -euxo pipefail

# Check if the role already exists
if echo "$create_role_output" | grep -q 'EntityAlreadyExists'; then
  echo "$GREENGRASS_V2_TOKEN_EXCHANGE_ROLE_NAME" already exists, skipping creation."
else
  echo "$create_role_output"
fi


# Create and attach an AWS IoT policy that allows your Greengrass
GREENGRASS_CORE_TOKEN_EXCHANGE_ROLE_ALIAS_POLICY_NAME=GreengrassCoreTokenExchangeRoleAliasPolicy
GREENGRASS_CORE_TOKEN_EXCHANGE_ROLE_ALIAS_POLICY_DOCUMENT='{
"Version":"2012-10-17",
  "Statement": [
    {
"Effect": "Allow",
      "Action": "iot:AssumeRoleWithCertificate",
      "Resource": "arn:aws:iot:'$AWS_REGION':'$AWS_ACCOUNT_NUMBER':rolealias/GreengrassCoreTokenExchangeRoleAlias"
    }
  ]
}'

# Create an AWS IoT policy from the policy document.
set +euxo pipefail
create_role_output=$(aws iot create-policy --policy-name "$GREENGRASS_CORE_TOKEN_EXCHANGE_ROLE_ALIAS_POLICY_NAME" --policy-document "$GREENGRASS_CORE_TOKEN_EXCHANGE_ROLE_ALIAS_POLICY_DOCUMENT" 2>&1)
set -euxo pipefail

# Check if the role already exists
if echo "$create_role_output" | grep -q 'EntityAlreadyExists'; then
  echo "$GREENGRASS_CORE_TOKEN_EXCHANGE_ROLE_ALIAS_POLICY_NAME already exists, skipping creation."
else
  echo "$create_role_output"
fi

#Attach the AWS IoT policy to the AWS IoT thing's certificate.
aws iot attach-policy --policy-name "$GREENGRASS_CORE_TOKEN_EXCHANGE_ROLE_ALIAS_POLICY_NAME" --target  `jq -r '.certificateArn' $NEW_GG_CORE_DEVICE/create-keys-and-certificate.json`


# Download certificates with private key and certificate files
# Download the Amazon root certificate authority (CA) certificate. AWS IoT certificates are associated with Amazon's root CA certificate by default.

curl -o $NEW_GG_CORE_DEVICE/AmazonRootCA1.pem https://www.amazontrust.com/repository/AmazonRootCA1.pem

cat << EOF > $NEW_GG_CORE_DEVICE/config.yaml
---
system:
    certificateFilePath: "/greengrass/v2/device.pem.crt"
    privateKeyPath: "/greengrass/v2/private.pem.key"
    rootCaPath: "/greengrass/v2/AmazonRootCA1.pem"
    rootpath: "/greengrass/v2"
    thingName: $NEW_GG_CORE_DEVICE
services:
    aws.greengrass.Nucleus:
        componentType: "NUCLEUS"
        configuration:
            awsRegion: "$AWS_REGION"
            iotRoleAlias: "GreengrassCoreTokenExchangeRoleAlias"      
            iotDataEndpoint: `aws --output text iot describe-endpoint --endpoint-type iot:Data-ATS`
            iotCredEndpoint: `aws --output text iot describe-endpoint --endpoint-type iot:CredentialProvider`
EOF

# Creating an example how the certs and config get onto the image
set +u
cat << EOF > $NEW_GG_CORE_DEVICE/copy_certs_to_image.sh
#!/bin/bash
# cmd to copy config and certs in an qemu image
MACHINE="qemuarm64"
#TAKEN from env IMAGE="aws-greengrass-test-image"

# wic cp does not overwrite
wic --debug rm tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/device.pem.crt 
wic --debug cp $NEW_GG_CORE_DEVICE/device.pem.crt tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/
wic --debug rm tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/private.pem.key
wic --debug cp $NEW_GG_CORE_DEVICE/private.pem.key tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/
wic --debug rm tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/AmazonRootCA1.pem
wic --debug cp $NEW_GG_CORE_DEVICE/AmazonRootCA1.pem tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/
wic --debug rm tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/config/config.yaml
wic --debug cp $NEW_GG_CORE_DEVICE/config.yaml tmp/deploy/images/\${MACHINE}/\${IMAGE}-\${MACHINE}.ext4:1/greengrass/v2/config/
EOF

echo copy config and certs:\$ bash $NEW_GG_CORE_DEVICE/copy_certs_to_image.sh