FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-6.5:"


SRC_URI:append = " file://odroid-kmeta;type=kmeta;name=odroid-kmeta;destsuffix=odroid-kmeta"


COMPATIBLE_MACHINE:odroid-m1s = "odroid-m1s"