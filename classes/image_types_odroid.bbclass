inherit image_types

#  -------------------------------------------
# | Area Name | From (sector #)| To (Sector #)|
# |-------------------------------------------|
# | SPL	      |  64	           | 1077         |
# | U-Boot	  |  2048	       | 6143         |
# | BOOT	  |  6144	       | 530431       |
# | rootfs	  |  530432	       | -            |
#  -------------------------------------------
#
IMAGE_CMD:wic:append() {
    dd if=${DEPLOY_DIR_IMAGE}/idblock.bin of=$out${IMAGE_NAME_SUFFIX}.wic conv=fsync bs=512 seek=64
    dd if=${DEPLOY_DIR_IMAGE}/uboot.img of=$out${IMAGE_NAME_SUFFIX}.wic conv=fsync bs=512 seek=2048
}
