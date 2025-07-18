#!/bin/bash
set -e

# Variables - modify these as needed
REGION=$(aws ec2 describe-availability-zones --output text --query 'AvailabilityZones[0].[RegionName]')
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
STACK_NAME="GreengrassFleetProvisioning"

# Calculate directories relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../../../.." && pwd)"
TEMP_DIR="${PROJECT_ROOT}/build/fleetprovisioning"

# Create build directory
mkdir -p ${TEMP_DIR}

echo "=== Setting up AWS IoT Fleet Provisioning for Greengrass ==="
echo "Region: ${REGION}"
echo "Account ID: ${ACCOUNT_ID}"
echo "Stack Name: ${STACK_NAME}"
echo "Temporary Directory: ${TEMP_DIR}"

# Deploy CloudFormation stack
echo -e "\n=== Deploying CloudFormation stack ==="
STACK_STATUS=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --region ${REGION} --query "Stacks[0].StackStatus" --output text 2>/dev/null || echo "DOES_NOT_EXIST")

if [ "$STACK_STATUS" == "ROLLBACK_COMPLETE" ] || [ "$STACK_STATUS" == "CREATE_FAILED" ] || [ "$STACK_STATUS" == "UPDATE_FAILED" ] || [ "$STACK_STATUS" == "UPDATE_ROLLBACK_COMPLETE" ]; then
  echo "Stack is in ${STACK_STATUS} state. Deleting it first..."
  aws cloudformation delete-stack --stack-name ${STACK_NAME} --region ${REGION}
  echo "Waiting for stack deletion to complete..."
  aws cloudformation wait stack-delete-complete --stack-name ${STACK_NAME} --region ${REGION}
  STACK_STATUS="DOES_NOT_EXIST"
fi

if [ "$STACK_STATUS" == "DOES_NOT_EXIST" ]; then
  echo "Creating new CloudFormation stack: ${STACK_NAME}"
  aws cloudformation create-stack \
    --stack-name ${STACK_NAME} \
    --template-body file://${SCRIPT_DIR}/fleet-provisioning-cfn.yaml \
    --capabilities CAPABILITY_NAMED_IAM \
    --region ${REGION}
  
  echo "Waiting for stack creation to complete..."
  aws cloudformation wait stack-create-complete --stack-name ${STACK_NAME} --region ${REGION}
else
  echo "Updating existing CloudFormation stack: ${STACK_NAME}"
  aws cloudformation update-stack \
    --stack-name ${STACK_NAME} \
    --template-body file://${SCRIPT_DIR}/fleet-provisioning-cfn.yaml \
    --capabilities CAPABILITY_NAMED_IAM \
    --region ${REGION} || echo "No updates are to be performed."
fi

# Get outputs from CloudFormation stack
echo -e "\n=== Getting CloudFormation stack outputs ==="
PROVISIONING_TEMPLATE_NAME=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query "Stacks[0].Outputs[?OutputKey=='ProvisioningTemplateName'].OutputValue" --output text --region ${REGION})
TOKEN_EXCHANGE_ROLE_ALIAS=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query "Stacks[0].Outputs[?OutputKey=='TokenExchangeRoleAlias'].OutputValue" --output text --region ${REGION})
THING_GROUP_NAME=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query "Stacks[0].Outputs[?OutputKey=='ThingGroupName'].OutputValue" --output text --region ${REGION})
MAC_VALIDATION_LAMBDA_ARN=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query "Stacks[0].Outputs[?OutputKey=='MacValidationLambdaArn'].OutputValue" --output text --region ${REGION})

echo "Provisioning Template Name: ${PROVISIONING_TEMPLATE_NAME}"
echo "Token Exchange Role Alias: ${TOKEN_EXCHANGE_ROLE_ALIAS}"
echo "Thing Group Name: ${THING_GROUP_NAME}"
echo "MAC Validation Lambda ARN: ${MAC_VALIDATION_LAMBDA_ARN}"

# Always create a new claim certificate
echo -e "\n=== Creating claim certificate ==="
echo "Creating new claim certificate..."
aws iot create-keys-and-certificate \
  --set-as-active \
  --certificate-pem-outfile "${TEMP_DIR}/certificate.pem.crt" \
  --private-key-outfile "${TEMP_DIR}/private.pem.key" \
  --region ${REGION} > ${TEMP_DIR}/cert-details.json

# Attach the fleet provisioning policy to the claim certificate
echo "Attaching FleetProvisioningPolicy to certificate..."
CERT_ARN=$(jq -r '.certificateArn' ${TEMP_DIR}/cert-details.json)
CERT_ID=$(jq -r '.certificateId' ${TEMP_DIR}/cert-details.json)
echo "Certificate ID: ${CERT_ID}"
aws iot attach-policy \
  --policy-name "FleetProvisioningPolicy-${STACK_NAME}" \
  --target "${CERT_ARN}" \
  --region ${REGION}

# Download the Amazon root CA certificate
echo -e "\n=== Downloading Amazon root CA certificate ==="
curl -s -o ${TEMP_DIR}/AmazonRootCA1.pem https://www.amazontrust.com/repository/AmazonRootCA1.pem

# Get IoT endpoints
echo -e "\n=== Getting IoT endpoints ==="
IOT_DATA_ENDPOINT=$(aws iot describe-endpoint --endpoint-type iot:Data-ATS --region ${REGION} --output text)
IOT_CRED_ENDPOINT=$(aws iot describe-endpoint --endpoint-type iot:CredentialProvider --region ${REGION} --output text)

echo "IoT Data Endpoint: ${IOT_DATA_ENDPOINT}"
echo "IoT Credential Endpoint: ${IOT_CRED_ENDPOINT}"

# Create local.conf snippet
echo -e "\n=== Creating local.conf snippet ==="
cat > ${TEMP_DIR}/local.conf.sample << EOF
# Fleet provisioning configuration
PACKAGECONFIG:append:pn-greengrass-lite = " fleetprovisioning"
AWS_REGION:pn-greengrass-lite = "${REGION}"
IOT_DATA_ENDPOINT:pn-greengrass-lite = "${IOT_DATA_ENDPOINT}"
IOT_CRED_ENDPOINT:pn-greengrass-lite = "${IOT_CRED_ENDPOINT}"
IOT_ROLE_ALIAS:pn-greengrass-lite = "${TOKEN_EXCHANGE_ROLE_ALIAS}"
FLEET_PROVISIONING_TEMPLATE:pn-greengrass-lite = "${PROVISIONING_TEMPLATE_NAME}"
FLEET_CLAIM_CERTS_PATH:pn-greengrass-lite = "\${TOPDIR}/../build/fleetprovisioning"
EOF

echo -e "\n=== Fleet provisioning setup complete ==="
echo "Files generated in: ${TEMP_DIR}"
echo "  - local.conf.sample"
echo "  - certificate.pem.crt"
echo "  - private.pem.key"
echo "  - AmazonRootCA1.pem"
echo ""
# Display certificate ID if available
if [ -f "${TEMP_DIR}/cert-details.json" ]; then
  CERT_ID=$(jq -r '.certificateId' ${TEMP_DIR}/cert-details.json)
  echo "Claim Certificate ID: ${CERT_ID}"
fi
echo ""
echo "To use fleet provisioning in your build:"
echo "1. Copy the contents of ${TEMP_DIR}/local.conf.sample to your local.conf"
echo "2. Build your image with fleet provisioning enabled"
echo ""
echo "The device will use the claim certificates to provision itself"
echo "and will be added to the ${THING_GROUP_NAME} thing group."
echo ""
echo "MAC Address Validation:"
echo "A pre-provisioning Lambda function has been configured to validate MAC addresses."
echo "When provisioning, use the device's MAC address as the SerialNumber."
echo "Valid MAC address formats: XX:XX:XX:XX:XX:XX, XX-XX-XX-XX-XX-XX, XX_XX_XX_XX_XX_XX or XXXXXXXXXXXX"
