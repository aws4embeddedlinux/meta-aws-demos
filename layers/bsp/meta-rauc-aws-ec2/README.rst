This README file contains information on the contents of the meta-rauc-aws-ec2 layer.

Please see the corresponding sections below for details.

Dependencies
============

* URI: git://git.openembedded.org/openembedded-core
* URI: https://github.com/rauc/meta-rauc.git
* URI: https://github.com/aws4embeddedlinux/meta-aws
* URI: https://github.com/lgirdk/meta-virtualization


Patches
=======

Please submit any patches against the meta-rauc-aws-ec2 layer via GitHub
pull request on https://github.com/rauc/meta-rauc-community.

Maintainer: Thomas Roos <throos@amazon.de>

Disclaimer
==========

Note that this is just an example layer that shows a few possible configuration
options how RAUC can be used.
Actual requirements may differ from project to projects and will probably need
a much different RAUC/bootloader/system configuration.

Also note that this layer is for demo purpose only and does not care about
migratability between different layer revision.

I. Adding the meta-rauc-aws-ec2 layer to your build
=======================================================

Run 'bitbake-layers add-layer meta-rauc-aws-ec2'

II. Build The Demo System
=========================

::

   $ source oe-init-build-env

Set the ``MACHINE`` to the model you intend to build for. E.g.::


   MACHINE = "aws-ec2-arm64"
   # or MACHINE = "aws-ec2-x86-64"

Make sure either your distro (recommended) or your local.conf have ``rauc``
``DISTRO_FEATURE`` enabled::

   DISTRO_FEATURES:append = " rauc"

Add this to your local.conf
   INHERIT += "aws-ec2-image"

Build the image::

   $ bitbake core-image-minimal

III. Flash & Run The Demo System
================================

Follow instructions here: https://github.com/aws4embeddedlinux/meta-aws/blob/master/scripts/ec2-ami/README.md

IV. Build and Install The Demo Bundle
=====================================

To build the bundle, run::

  $ bitbake ec2-demo-bundle

Copy the generated bundle to the target system via scp.

On the target, you can then install the bundle::

  # rauc install /path/to/ec2-demo-bundle.raucb
