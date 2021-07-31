# Demonstrations for the **[meta-aws](https://github.com/aws/meta-aws)** project

[meta-aws](https://github.com/aws/meta-aws) is a [Yocto
Project](https://www.yoctoproject.org/) Bitbake Metadata Layer. It
accelerates building [Amazon Web Services](https://aws.amazon.com)
(AWS) software you can install to [Embedded
Linux](https://elinux.org/Main_Page). Customers use this to build IoT
solutions on AWS.

In this repository, you will find
[meta-aws](https://github.com/aws/meta-aws) demonstrations.  These
demonstrations are both Poky (Yocto Project reference implementation)
based and real hardware based.  Many times, the hardware will be
representative of real use of hardware listed in the [AWS Device
Catalog](https://devices.amazonaws.com).

The number of demonstrations will increase over time and your
contribution is very welcome!

## Demonstration environments

Select your desired target environment.  For more information how this
repository is structured see the next section.

These are listed in alphabetical order for ease of selection and
should in no way infer preference.

- [NXP](nxp/README.md)
- [Raspberry Pi Foundation](rpi_foundation/README.md)
- [Renesas](renesas/README.md)
- [Texas Instruments](ti/README.md)
- [UP Board](up/README.md)
- [Xilinx](xilinx/README.md)


## How this repository is organized

The demonstrations in this repository can be categorized as either an
AWS service example or a use case example.  AWS service examples are
usually setup as basic `local.conf` driven examples on existing
reference distributions like Yocto Project's Poky. Use case examples
are usually more complete with a reference distribution for a target
EVB since they may have peripheral configuration specific to the use
case.

Every example has the following properties.

- Target operating environment (emulation, virtualization, or physical
  evaluation or development board)
- Included AWS device software

Every example has a build status that is derived by an AWS CodeBuild
outcome.

- Target Yocto Project or Automotive Grade Linux release

```text
Main
 \_Semiconductor, Device Manufacturer, or Emulator/Simulator
   \_Device or Operating Environment, and bitness if applicable.
     \_Software or Use Case page
```

© 2020-2021, Amazon Web Services, Inc. or its affiliates. All rights reserved.
