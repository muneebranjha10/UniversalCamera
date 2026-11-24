#!/system/bin/sh
ui_print 'Samsung Camera Enhancement 2.1 experimental | @s23_hype'
[ "$(getprop ro.product.manufacturer | tr '[:upper:]' '[:lower:]')" = samsung ] || abort 'Samsung devices only.'
[ -f "$MODPATH/companion.apk" ] || abort 'Missing compiled LSPosed companion.'
touch "$MODPATH/skip_mount"
[ "$BOOTMODE" = true ] || abort 'Install from a running Android root manager.'
ui_print 'Installing LSPosed companion (no Camera data reset)...'
result=$(pm install -r "$MODPATH/companion.apk" 2>&1)
ui_print "$result"
echo "$result" | grep -q '^Success' || abort 'Companion install failed. Check signature or install companion manually.'
ui_print 'Enable Camera Enhancement in LSPosed and select Samsung Camera only. Reboot.'
