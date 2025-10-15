# aws-iot-greengrass-lite-container-demo-image

This image is similar to [aws-iot-greengrass-lite-demo-image](../aws-iot-greengrass-lite-demo-image/README.md)

It will create a 43MB systemd libmusl container image with greengrass lite installed.


## BUILDING

Init build environment

```bash
. init-build-env
```

Configure this image

```bash
export IMAGE=aws-iot-greengrass-lite-container-demo-image
```

Configure a device e.g. qemuarm64

```bash
export DEVICE=qemuarm64
```

Build

```bash
bitbake $IMAGE
```

You can run an interactive terminal like this - password root:root :

```bash
podman run -it --arch arm64 oci:/home/ubuntu/data/meta-aws-demos/build/tmp/deploy/images/qemuarm64/aws-iot-greengrass-lite-container-demo-image-latest-oci/ /bin/bash
```

This also works with fleetprovisioning, if certs are not in the docker image, you can mount them into the container e.g.:

```bash
podman run -it --arch arm64 -v /host/path:/container/path oci:/home/ubuntu/data/meta-aws-demos/build/tmp/deploy/images/qemuarm64/aws-iot-greengrass-lite-container-demo-image-latest-oci/ /bin/bash
```

To start and stop multiple instances of a container

```bash
# Start 20 instances (see hitting a limit on issues):
for i in {1..20}; do podman run -d --name greengrass-$i --arch arm64 oci:/home/ubuntu/data/meta-aws-demos/build/tmp/deploy/images/qemuarm64/aws-iot-greengrass-lite-container-demo-image-latest-oci/; done

# Stop all instances:
podman stop $(podman ps -q --filter name=greengrass-)

# Remove all instances:
podman rm $(podman ps -aq --filter name=greengrass-)

# attach to a instance a terminal
podman exec -it greengrass-1 /bin/bash

# List all instances:
podman ps --filter name=greengrass-

# Get detailed info with resource usage:
podman stats --no-stream --filter name=greengrass-

# Show all container details:
podman inspect greengrass-1

# Quick health check all instances:
for i in {1..10}; do echo "=== greengrass-$i ==="; podman exec greengrass-$i ps aux | head -5; done

```

### how to detach from a instance?
Press Ctrl+P then Ctrl+Q to detach from the container without stopping it.


### hitting a limit in starting containers

```
# Defines the maximum number of inotify listeners.
# By default, this value is 128, which is quickly exhausted when using
# systemd-based LXC containers (15 containers are enough).
# When the limit is reached, systemd becomes mostly unusable, throwing
# "Too many open files" all around (both on the host and in containers).
# See https://kdecherf.com/blog/2015/09/12/systemd-and-the-fd-exhaustion/
# Increase the user inotify instance limit to allow for about
# 100 containers to run before the limit is hit again
fs.inotify.max_user_instances = 1024
So you should do the same by creating this file on the host. For immediate effect (on the host):

sysctl -w fs.inotify.max_user_instances=1024

or permanently add or change here: /etc/sysctl.conf
fs.inotify.max_user_instances = 1024

```