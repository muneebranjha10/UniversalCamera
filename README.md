# UniversalCamera | @s23_hype

Experimental Samsung Camera enhancement 2.1. Builds a Samsung-only LSPosed companion APK and KernelSU/Magisk ZIP. No proprietary Samsung APK or assets are included.

## Build
Open Actions > Build Camera Enhancement APK. Download the artifact after a successful run. It contains the debug APK and full flashable bundle. The ZIP installs the APK; LSPosed must already work. Enable the companion and scope only Samsung Camera, then reboot. Remove previous camera modules before testing. Disable the companion if Camera fails; root manager removal does not uninstall the companion.

## Limits
The JSON adapter adds only modes already shipped by the installed Camera and supported by reported normal camera/encoder capabilities. Shared menus use intersection across exposed front/rear cameras. XML adapter redirects recognized existing XML flags in the Camera process. Higher Pro Video FPS, HDR and flash require supported hardware and matching firmware paths; none are guaranteed. High-speed-only modes are reported, not inserted into normal Pro Video sessions. Unknown firmware remains stock. Actual recording and physical device compatibility are unverified. This is not a universal release. No camera settings reset, no native HAL patches.
