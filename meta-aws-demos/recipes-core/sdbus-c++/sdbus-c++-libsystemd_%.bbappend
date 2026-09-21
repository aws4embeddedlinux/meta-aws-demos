# Stopgap: meta-oe's sdbus-c++-libsystemd recipe does not tell meson which
# libc to target, so systemd's in-tree musl support is disabled and the static
# libsystemd.a build fails on musl (missing printf.h). Enable it here until the
# fix lands upstream. See openembedded/meta-openembedded#1071.
EXTRA_OEMESON += "-Dlibc=${TCLIBC}"
