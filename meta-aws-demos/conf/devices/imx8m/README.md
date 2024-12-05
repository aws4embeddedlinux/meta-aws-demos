# Build for the NXP i.MX8MQEVK and i.MX8MPEVK

The whole image can be build with this
```bash
export DEVICE=imx8m
bitbake imx-image-full
```

> [!NOTE]
> this demo will require to accept the EULA in the config manually.
> add ACCEPT_FSL_EULA = "1" in your local.conf
