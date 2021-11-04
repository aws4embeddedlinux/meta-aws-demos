# TI Jacinto Getting Started Guide for AWS IoT Greengrass V2

## Table of Contents
>1	Document Information	
>
>2	Overview	
>
>3	Hardware Description	
>
>4	Set up your hardware	
>
>5	Setup your AWS account and Permissions	
>
>6	Create Resources for your device in AWS IoT	
>
>7	Install the AWS Command Line Interface	
>
>8	Install AWS IoT Greengrass	
>
>9	Create a "Hello World" component	
>
>10	Troubleshooting	

## 1	Document Information
Note that all instructions in this document have been written assuming a Linux host machine.  
### 1.1	Revision History
04-Oct-2021	Initial Draft
## 2	Overview
This guide covers both the J721EXSOMG01EVM and J721EXSKG01EVM boards.

### 2.1	J721EXSOMG01EVM 
The J721EXSOMG01EVM system-on-module-when paired with the J721EXPCP01EVM common 
processor board-lets you evaluate TDA4VM and DRA829V processors in vision analytics and networking 
applications throughout automotive and industrial markets. These processors perform particularly well in 
surround view camera and automated parking applications, as well as in vehicle compute gateway 
applications where software-as-a-service models are deployed.

TDA4VM and DRA829V processors have a powerful, heterogeneous architecture that includes a mix of 
fixed and floating-point DSP cores, Arm(r) Cortex(r)-A72 cores, matrix math acceleration for machine 
learning, integrated ISP and vision processing acceleration, 2D and 3D GPU cores, and H.264 
encode/H.265 decode acceleration.

An integrated Safety MCU includes dual-lockstep R5F cores that help the system achieve up to ASIL-D 
safety integration and integrated peripherals allow for multi-camera input via CSI-2 ports, vehicle 
connectivity based on PCI Express, CAN-FD and Gigabit Ethernet, and display connectivity via DSI 
interfaces. Gigabit Ethernet and PCIe switches are also available.

The EVM is supported by Processor SDK Linux and RTOS, as well as additional components such as Edge AI 
and Robotics SDK which includes foundational drivers, compute and vision kernels, and example 
application frameworks and demonstrations that show you how to take advantage of the powerful, 
heterogeneous architecture of Jacinto 7 processors.

This guide describes how to get started with AWS IoT GreengrassV2 on your board, thus enabling easier 
deployment and management of your devices, applications and ML models at the edge.

### 2.2	J721EXSKG01EVM
The J721EXSKG01EVM is a low-cost TDA4VM processor starter kit. The kit enables 8 TOPS of deep 
learning performance and hardware-accelerated edge AI processing.  For more details, refer to the 
[description](https://www.ti.com/tool/SK-TDA4VM#description).

## 3 About AWS IoT GreengrassV2
For more details about GreengrassV2, see [how it works](https://docs.aws.amazon.com/greengrass/v2/developerguide/how-it-works.html) and [what's new](https://docs.aws.amazon.com/greengrass/v2/developerguide/greengrass-v2-whats-new.html).

## 4	Hardware Description
### 4.1	DataSheet
For the J721EXSOMG01EVM, refer to the [Quick Start Guide](https://www.ti.com/lit/pdf/SPRUIS8) and the [User's Guide](https://www.ti.com/lit/pdf/spruis4) for important 
information regarding setting up the board as well as technical specifications.

For the J721EXSKG01EVM, refer to the [Quick Start Guide](http://software-dl.ti.com/jacinto7/esd/processor-sdk-linux-sk-tda4vm/latest/exports/docs/getting_started.html) and the [User's Guide](https://www.ti.com/lit/pdf/spruj21) for important information regarding setting up the board as well as technical specifications.

### 4.2	Standard Kit Contents
#### 4.2.1	J721EXSOMG01EVM 
The J721EXSOMG01EVM comes in several orderable configurations.  Refer to the [details](https://www.ti.com/tool/J721EXSOMXEVM#order-start-development) and choose 
the appropriate package for your development needs.

Details about the corresponding SDKs are also available in the package description pages.

#### 4.2.2	J721EXSKG01EVM 
Ordering and other details for the J721EXSKG01EVM are available [here](https://www.ti.com/tool/SK-TDA4VM#order-start-development).

### 4.3	User Provided items
#### 4.3.1	J721EXSOMG01EVM 
The user has to provide the following:
1. Ethernet cable
2. Router with access to the Internet
3. Power supply (see 3rd Party purchasable items section)

#### 4.3.2	J721EXSKG01EVM 
The user has to provide the following:
1. Ethernet cable
2. Router with access to the Internet
3. Power supply (see 3rd Party purchasable items section)

### 4.4	3rd Party purchasable items
#### 4.4.1	Power Supply
The J721EXSOMG01EVM does not ship with a power supply.  Power supplies are available from [Digikey](https://www.digikey.com/en/products/detail/qualtek/QADC-65-20-08CB/9771104).  
For more details, refer to the [User Guide](https://www.ti.com/lit/pdf/spruis4).

The J721EXSKG01EVM does not ship with a power supply.  Power supplies are available from Digikey.  You 
will need to order a cord separately.  For more details, refer to the [User Guide](https://www.ti.com/lit/pdf/spruj21).

### 4.5	Additional References
More information about the TDA4VM and DRA829V can be found [here](https://www.ti.com/tool/J721EXSOMXEVM#supported-products).

## 5	Set up your hardware
Instructions on setting up the J721EXSOMG01EVM board can be found [here](https://software-dl.ti.com/jacinto7/esd/processor-sdk-rtos-jacinto7/08_00_00_12/exports/docs/psdk_rtos/docs/user_guide/evm_setup_j721e.html).

To set up the J721EXSKG01EVM, refer to the instructions [here](https://software-dl.ti.com/jacinto7/esd/edgeai-sdk/latest/exports/docs/getting_started.html).

## 6	Setup your AWS account and Permissions
Refer to the instructions at [Set up your AWS Account](https://docs.aws.amazon.com/iot/latest/developerguide/setting-up.html).  Follow the steps outlined in these sections to create your account and a user and get started:
*	Sign up for an AWS account and 
*	Create a user and grant permissions. 
*	Open the AWS IoT console

Pay special attention to the Notes.

## 7	Create Resources for your device in AWS IoT
Refer to the instructions at [Create AWS IoT Resources](https://docs.aws.amazon.com/iot/latest/developerguide/create-iot-resources.html).  Follow the steps outlined in these sections to provision resources for your device:
*	Create an AWS IoT Policy
*	Create a thing object 

Pay special attention to the Notes.

## 8	Install the AWS Command Line Interface
To install the AWS CLI on your host machine, refer to the instructions at [Installing the AWS CLI v2](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html).  
Installing the CLI is needed to complete the instructions in this guide.

## 9	Install AWS IoT Greengrass
Follow the detailed instructions at [Install Greengrass v2 on Jacinto](https://aws-edge-iot-ml.workshop.aws/en/spells/install-ggv2-j7.html) to set up, install and verify AWS IoT Greengrass on your device.

## 10	Create a "Hello World" component
In Greengrass v2, components can be created on the edge device and uploaded to the cloud, or vice versa.

### 10.1	Create the component on your edge device
Follow the instructions online under the section [Create your first component](https://docs.aws.amazon.com/greengrass/v2/developerguide/getting-started.html#create-first-component) to create, deploy, test, update and manage a simple component on your device.

### 10.2	Upload the component to the cloud
Follow the instructions online at [Upload your component](https://docs.aws.amazon.com/greengrass/v2/developerguide/getting-started.html#upload-first-component) to upload your component to the cloud, where it can be deployed to other devices as needed.

## 11	Troubleshooting
Refer to the instructions at [Troubleshooting Greengrass v2](https://docs.aws.amazon.com/greengrass/v2/developerguide/troubleshooting.html) for information on:
- How to View AWS IoT Greengrass Core software logs
- How to View component logs
- AWS IoT Greengrass Core software issues
- AWS IoT Greengrass cloud issues
- Core device deployment issues
- Core device component issues

You can also refer to [Logging and Monitoring](https://docs.aws.amazon.com/greengrass/v2/developerguide/logging-and-monitoring.html) to learn how to log API calls, gather system health telemetry data, and check core device status.
