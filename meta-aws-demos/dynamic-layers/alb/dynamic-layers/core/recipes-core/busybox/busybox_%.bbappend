# otherwise no systemd init.cfg is used always, mean /sbin/init is pointing at busybox, by this modification this is only using init.cfg when busybox is enabled, as it is in done in poky layer.
SRC_URI:remove = " file://init.cfg"
SRC_URI:append = "${@["", " file://init.cfg"][(d.getVar('VIRTUAL-RUNTIME_init_manager') == 'busybox')]}"
