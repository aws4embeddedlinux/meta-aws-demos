#!/bin/bash
set -e

# Variables - modify these as needed
REGION=$(aws ec2 describe-availability-zones --output text --query 'AvailabilityZones[0].[RegionName]')
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
TEMPLATE_NAME="FleetTestNew"
ROLE_ARN="arn:aws:iam::${ACCOUNT_ID}:role/FleetProvisioningRole"
POLICY_NAME="FleetProvisioningPolicy"
SERIAL_NUMBER="AAA55555"
# Calculate build directory relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../../../.." && pwd)"
TEMP_DIR="${PROJECT_ROOT}/build/fleetprovisioning"
LAMBDA_FUNCTION_NAME="FleetProvisioningHook"
LAMBDA_ROLE_NAME="FleetProvisioningLambdaRole"
LAMBDA_POLICY_NAME="FleetProvisioningLambdaPolicy"

# Create build directory
mkdir -p ${TEMP_DIR}

# Edit this Create template file as required
cat > ${TEMP_DIR}/template.json << 'EOF'
{
  "Parameters": {
    "SerialNumber": {
      "Type": "String"
    },
    "AWS::IoT::Certificate::Id": {
      "Type": "String"
    }
  },
  "Resources": {
    "policy_GreengrassV2TokenExchangeCoreDeviceRoleAliasPolicy": {
      "Type": "AWS::IoT::Policy",
      "Properties": {
        "PolicyName": "GreengrassV2TokenExchangeCoreDeviceRoleAliasPolicy"
      }
    },
    "policy_GreengrassV2IoTThingPolicy": {
      "Type": "AWS::IoT::Policy",
      "Properties": {
        "PolicyName": "GreengrassV2IoTThingPolicy"
      }
    },
    "certificate": {
      "Type": "AWS::IoT::Certificate",
      "Properties": {
        "CertificateId": {
          "Ref": "AWS::IoT::Certificate::Id"
        },
        "Status": "Active"
      }
    },
    "thing": {
      "Type": "AWS::IoT::Thing",
      "OverrideSettings": {
        "AttributePayload": "MERGE",
        "ThingGroups": "DO_NOTHING",
        "ThingTypeName": "REPLACE"
      },
      "Properties": {
        "AttributePayload": {},
        "ThingGroups": [
          "NewGreengrassDevices"
        ],
        "ThingName": {
          "Fn::Join": [
            "",
            [
              "greengrass",
              {
                "Ref": "SerialNumber"
              }
            ]
          ]
        }
      }
    }
  }
}
EOF

# Check if role exists
if ! aws iam get-role --role-name FleetProvisioningRole &>/dev/null; then
  echo "Creating FleetProvisioningRole..."
  aws iam create-role --role-name FleetProvisioningRole --assume-role-policy-document file://${SCRIPT_DIR}/fleet-provisioning-trust-policy.json
else
  echo "Role FleetProvisioningRole already exists"
  # Update the trust policy
  aws iam update-assume-role-policy --role-name FleetProvisioningRole --policy-document file://${SCRIPT_DIR}/fleet-provisioning-trust-policy.json
fi

# Check if policy exists
if ! aws iam get-policy --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/FleetProvisioningPolicy &>/dev/null; then
  echo "Creating FleetProvisioningPolicy..."
  aws iam create-policy --policy-name FleetProvisioningPolicy --policy-document file://${SCRIPT_DIR}/fleet-provisioning-policy.json
else
  echo "Policy FleetProvisioningPolicy already exists, updating..."
  # Delete old policy versions except the default one
  for version in $(aws iam list-policy-versions --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/FleetProvisioningPolicy --query 'Versions[?IsDefaultVersion==`false`].VersionId' --output text); do
    aws iam delete-policy-version --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/FleetProvisioningPolicy --version-id "$version"
  done
  # Create a new policy version
  aws iam create-policy-version --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/FleetProvisioningPolicy \
    --policy-document file://${SCRIPT_DIR}/fleet-provisioning-policy.json \
    --set-as-default || echo "Policy update failed, continuing..."
fi

# Ensure the policy is attached to the role
echo "Ensuring policy is attached to role..."
aws iam detach-role-policy --role-name FleetProvisioningRole --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/FleetProvisioningPolicy &>/dev/null || true
aws iam attach-role-policy --role-name FleetProvisioningRole --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/FleetProvisioningPolicy

# Create thing group if it doesn't exist
if ! aws iot describe-thing-group --thing-group-name NewGreengrassDevices --region ${REGION} &>/dev/null; then
  aws iot create-thing-group --thing-group-name NewGreengrassDevices --region ${REGION}
else
  echo "Thing group NewGreengrassDevices already exists, skipping creation"
fi

# Create IoT policy if it doesn't exist
echo "Creating IoT policy if it doesn't exist..."
if ! aws iot get-policy --policy-name "${POLICY_NAME}" --region ${REGION} &>/dev/null; then
  aws iot create-policy \
    --policy-name "${POLICY_NAME}" \
    --policy-document file://${SCRIPT_DIR}/iot-policy.json \
    --region ${REGION}
else
  echo "Policy ${POLICY_NAME} already exists, skipping creation"
fi

# Create Lambda execution role if it doesn't exist
echo "Creating Lambda execution role if it doesn't exist..."
if ! aws iam get-role --role-name ${LAMBDA_ROLE_NAME} &>/dev/null; then
  aws iam create-role \
    --role-name ${LAMBDA_ROLE_NAME} \
    --assume-role-policy-document file://${SCRIPT_DIR}/lambda-execution-role-policy.json
  
  # Create Lambda permission policy if it doesn't exist
  if ! aws iam get-policy --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/${LAMBDA_POLICY_NAME} &>/dev/null; then
    aws iam create-policy \
      --policy-name ${LAMBDA_POLICY_NAME} \
      --policy-document file://${SCRIPT_DIR}/lambda-permission-policy.json
  fi
  
  # Attach policy to role
  aws iam attach-role-policy \
    --role-name ${LAMBDA_ROLE_NAME} \
    --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/${LAMBDA_POLICY_NAME}
else
  echo "Lambda execution role ${LAMBDA_ROLE_NAME} already exists"
fi

# Wait for IAM changes to propagate
echo "Waiting for IAM changes to propagate..."
sleep 10

# Create Lambda function if it doesn't exist
echo "Creating Lambda function if it doesn't exist..."
if ! aws lambda get-function --function-name ${LAMBDA_FUNCTION_NAME} --region ${REGION} &>/dev/null; then
  # Zip the Lambda function code
  echo "Zipping Lambda function code..."
  cd ${SCRIPT_DIR} && zip -j ${TEMP_DIR}/lambda_function.zip lambda_function.py > /dev/null
  
  # Create the Lambda function
  echo "Creating Lambda function..."
  aws lambda create-function \
    --function-name ${LAMBDA_FUNCTION_NAME} \
    --runtime python3.9 \
    --role arn:aws:iam::${ACCOUNT_ID}:role/${LAMBDA_ROLE_NAME} \
    --handler lambda_function.handler \
    --zip-file fileb://${TEMP_DIR}/lambda_function.zip \
    --region ${REGION} > /dev/null
else
  echo "Lambda function ${LAMBDA_FUNCTION_NAME} already exists, updating..."
  # Update the Lambda function code
  cd ${SCRIPT_DIR} && zip -j ${TEMP_DIR}/lambda_function.zip lambda_function.py > /dev/null
  aws lambda update-function-code \
    --function-name ${LAMBDA_FUNCTION_NAME} \
    --zip-file fileb://${TEMP_DIR}/lambda_function.zip \
    --region ${REGION} > /dev/null
fi

# Add permission to Lambda function to allow IoT to invoke it
echo "Adding permission to Lambda function..."
aws lambda add-permission \
  --function-name ${LAMBDA_FUNCTION_NAME} \
  --source-arn arn:aws:iot:${REGION}:${ACCOUNT_ID}:provisioningtemplate/${TEMPLATE_NAME} \
  --statement-id IoTProvisioningAccess \
  --action lambda:InvokeFunction \
  --principal iot.amazonaws.com \
  --region ${REGION} || echo "Lambda permission already exists or couldn't be added"

# Create the fleet provisioning template if it doesn't exist
echo "Creating fleet provisioning template if it doesn't exist..."
if ! aws iot describe-provisioning-template --template-name "${TEMPLATE_NAME}" --region ${REGION} &>/dev/null; then
  aws iot create-provisioning-template \
    --template-name "${TEMPLATE_NAME}" \
    --provisioning-role-arn "${ROLE_ARN}" \
    --template-body file://${SCRIPT_DIR}/template_with_condition.json \
    --region ${REGION}
else
  echo "Provisioning template ${TEMPLATE_NAME} already exists, updating..."
  aws iot update-provisioning-template \
    --template-name "${TEMPLATE_NAME}" \
    --provisioning-role-arn "${ROLE_ARN}" \
    --template-body file://${SCRIPT_DIR}/template_with_condition.json \
    --region ${REGION} || echo "Template update failed, continuing..."
fi

# Check if certificate files already exist
if [ ! -f "${TEMP_DIR}/certificate.pem.crt" ] || [ ! -f "${TEMP_DIR}/private.pem.key" ]; then
  # Create a claim certificate
  echo "Creating claim certificate..."
  aws iot create-keys-and-certificate \
    --set-as-active \
    --certificate-pem-outfile "${TEMP_DIR}/certificate.pem.crt" \
    --private-key-outfile "${TEMP_DIR}/private.pem.key" \
    --region ${REGION} > ${TEMP_DIR}/cert-details.json

  # Attach the fleet provisioning policy to the claim certificate
  echo "Attaching policy to certificate..."
  CERT_ARN=$(jq -r '.certificateArn' ${TEMP_DIR}/cert-details.json)
  aws iot attach-policy \
    --policy-name "${POLICY_NAME}" \
    --target "${CERT_ARN}" \
    --region ${REGION}
else
  echo "Certificate files already exist, skipping certificate creation"
fi

# Download the Amazon root CA certificate
echo "Downloading Amazon root CA certificate..."
curl -s -o ${TEMP_DIR}/AmazonRootCA1.pem https://www.amazontrust.com/repository/AmazonRootCA1.pem

# Get IoT endpoints
echo "Getting IoT endpoints..."
IOT_DATA_ENDPOINT=$(aws iot describe-endpoint --endpoint-type iot:Data-ATS --region ${REGION} --output text)
IOT_CRED_ENDPOINT=$(aws iot describe-endpoint --endpoint-type iot:CredentialProvider --region ${REGION} --output text)

# Create local.conf snippet
cat > ${TEMP_DIR}/local.conf.sample << EOF
# Fleet provisioning configuration
PACKAGECONFIG:append:pn-greengrass-lite = " fleetprovisioning"
IOT_DATA_ENDPOINT:pn-greengrass-lite = "${IOT_DATA_ENDPOINT}"
IOT_CRED_ENDPOINT:pn-greengrass-lite = "${IOT_CRED_ENDPOINT}"
IOT_ROLE_ALIAS:pn-greengrass-lite = "GreengrassV2TokenExchangeRoleAlias"
FLEET_PROVISIONING_TEMPLATE:pn-greengrass-lite = "${TEMPLATE_NAME}"
FLEET_CLAIM_CERTS_PATH:pn-greengrass-lite = "\${TOPDIR}/../build/fleetprovisioning"
EOF

echo "Fleet provisioning setup complete."
echo "Files generated in: ${TEMP_DIR}"
echo "  - local.conf.sample"
echo "  - certificate.pem.crt"
echo "  - private.pem.key"
echo "  - AmazonRootCA1.pem"
echo ""
echo "To use fleet provisioning in your build:"
echo "1. Copy the contents of ${TEMP_DIR}/local.conf.sample to your local.conf"

# Cleanup generated files
echo -e "\nCleaning up temporary files..."
rm -f ${TEMP_DIR}/lambda_function.zip ${TEMP_DIR}/template.json
