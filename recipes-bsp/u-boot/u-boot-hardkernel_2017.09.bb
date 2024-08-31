require recipes-bsp/u-boot/u-boot.inc
DESCRIPTION = "Odroid m1s boot loader supported by the hardkernel product"
SECTION = "bootloaders"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6"

PROVIDES += "virtual/bootloader u-boot"

LIC_FILES_CHKSUM = "file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-2017.09:"

UBOOT_INITIAL_ENV = ""



SRC_URI = "git://github.com/hardkernel/u-boot.git;name=uboot;destsuffix=git/uboot;protocol=https;branch=odroidm1-v2017.09 \
	git://github.com/hardkernel/rk3568_rkbin.git;name=rkbin;destsuffix=git/rkbin;protocol=https;branch=odroidm1-v2017.09 \
	"
SRC_URI += "file://rockchip-scripts.sh"

SRCREV_uboot = "eb6940fb0ccb802d2001568a4e8030fbea07fb6a"
SRCREV_rkbin = "0737c2cdd2a1dc9774e77e59dd33fe7b20a839e4"
SRCREV_FORMAT = "uboot_rkbin"


PR = "${PV}+git${SRCPV}"

DEPENDS = " python3-native gcc libgcc bc-native coreutils-native "
UBOOT_SUFFIX ?= "bin"

PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${WORKDIR}/git/uboot"
RK = "${WORKDIR}/git/rkbin"
B = "${S}"

inherit uboot-boot-scr

EXTRA_OEMAKE += ' CC="${TARGET_PREFIX}gcc --sysroot=${RECIPE_SYSROOT} -Wno-maybe-uninitialized -Wno-enum-int-mismatch" '

do_configure () {
	cp -a ${RK} ${S}
	cd ${S}
	###############################################################
	# Ensure python2 is available since SPL_FIT_GENERATOR need it
	###############################################################
	if [ ! -e "${RECIPE_SYSROOT_NATIVE}/usr/bin/python2" ]; then
  	ln -s ${RECIPE_SYSROOT_NATIVE}/usr/bin/python3-native/python3 ${RECIPE_SYSROOT_NATIVE}/usr/bin/python2
	fi
	oe_runmake ${UBOOT_MACHINE}
}

do_compile () {
	cd ${S}
	oe_runmake all

	###############################################################
	# Source rockchip scripts for yocto
	###############################################################
	. ${WORKDIR}/rockchip-scripts.sh

	###############################################################
	# Creates the idblock.bin
	###############################################################
	./tools/mkimage -n rk3568 -T rksd -d rkbin/bin/rk35/rk3566_ddr_1056MHz_v1.18.bin:spl/u-boot-spl.bin idblock.bin

	###############################################################
	# Generates u-boot.its file (Image Tree Source) and u-boot.itb (Image Tree Binary)
  # ./make.sh itb rkbin/RKTRUST/RK3568TRUST.ini
	###############################################################
	export INI_TRUST="${S}/rkbin/RKTRUST/RK3568TRUST.ini"
	export INI_LOADER="${S}/rkbin/RKBOOT/RK3568-ODROIDM1S.ini"
	export RKBIN_TOOLS="${S}/rkbin/tools"
	export ARM64_TRUSTZONE=""
	export RKBIN=""
	export PLAT_TYPE=""
	export FIT_DIR="fit"
	export ITB_UBOOT="${FIT_DIR}/uboot.itb"
	export ITS_UBOOT="u-boot.its"
	export IMG_UBOOT="uboot.img"
	export ARG_VER_UBOOT="0"
	###############################################################
	# Read in some variables
	###############################################################
	prepare

	###############################################################
	# create Itb image and pack loader No siging.
	# see function fit_gen_uboot_itb() on fit-core.sh
	###############################################################
	# offs
	if grep -q '^CONFIG_FIT_ENABLE_RSA4096_SUPPORT=y' .config ; then
		export OFFS_DATA="0x1200"
	else
		export OFFS_DATA="0x1000"
	fi

  pack_uboot_itb_image

	mkdir -p ${FIT_DIR}

	./tools/mkimage -f ${ITS_UBOOT} -E -p ${OFFS_DATA} ${ITB_UBOOT} -v ${ARG_VER_UBOOT}

	pack_loader_image

  ###############################################################

	###############################################################
	# Using previous files generate uboot_img
	###############################################################
	fit_gen_uboot_img
	###############################################################

	####
	# sudo dd if=idblock.bin of=<DEVICE/NODE/OF/YOUR/STORAGE> conv=fsync seek=64
  # sudo dd if=uboot.img of=<DEVICE/NODE/OF/YOUR/STORAGE> conv=fsync seek=2048
}

do_deploy:append() {
	install -d ${DEPLOYDIR}
	install -m 755 ${B}/idblock.bin ${DEPLOYDIR}/idblock.bin
	install -m 755 ${B}/uboot.img ${DEPLOYDIR}/uboot.img
	install -m 755 ${WORKDIR}/${UBOOT_ENV_BINARY} ${DEPLOYDIR}/${UBOOT_ENV_BINARY}
}

COMPATIBLE_MACHINE = "odroid-m1s"
