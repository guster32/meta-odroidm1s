FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-6.1:"


SRC_URI:append = " file://odroid-kmeta;type=kmeta;name=odroid-kmeta;destsuffix=odroid-kmeta"
SRC_URI:append = " file://odroid/odroid-arm64.scc"


COMPATIBLE_MACHINE:odroid-m1s = "odroid-m1s"
