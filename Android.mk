LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, java)
LOCAL_ASSET_FILES += $(call find-subdir-assets)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += frameworks/support/v7/appcompat/res

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v7-appcompat android-support-v4 android-support-v13 netamaplib \
    amaplocationlib amap2dmaplib

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat

LOCAL_PACKAGE_NAME := UserCenter_IUV
LOCAL_PROGUARD_ENABLED:= disabled
LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false
LOCAL_PRIVILEGED_MODULE := true
LOCAL_USE_FRAMEWORK_GOME:= true

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := netamaplib:libs/AMap2DMap_5.2.0_AMapSearch_5.2.1_20170630.jar \
    amaplocationlib:libs/AMap_Location_V3.4.1_20170629.jar \
    amap2dmaplib:libs/Amap_2DMap_V5.2.0_20170627.jar
include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))