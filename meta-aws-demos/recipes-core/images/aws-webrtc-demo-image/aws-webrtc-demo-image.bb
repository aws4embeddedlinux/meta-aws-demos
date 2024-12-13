SUMMARY = "A image to test amazon-kvs-webrtc-sdk"
inherit core-image

IMAGE_INSTALL =+ "amazon-kvs-webrtc-sdk"

EXTRA_IMAGE_FEATURES ?= "allow-empty-password allow-root-login empty-root-password"

IMAGE_INSTALL =+ "gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-ugly tmux"
