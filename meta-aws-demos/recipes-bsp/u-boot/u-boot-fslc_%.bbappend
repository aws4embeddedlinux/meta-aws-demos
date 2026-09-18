# wrynose build fix: U-Boot 2025.01's binman does `import pkg_resources`, but
# wrynose ships setuptools 82.0.1, which removed pkg_resources (setuptools >= 80),
# so binman fails do_compile with "ModuleNotFoundError: No module named
# 'pkg_resources'". Patch binman to use importlib.resources (already imported in
# control.py) instead. This is an upstream meta-freescale / U-Boot gap
# (u-boot-fslc 2025.01 not yet setuptools-82 ready); drop once fixed upstream.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += "file://0001-binman-use-importlib-resources-instead-of-pkg_resour.patch"
