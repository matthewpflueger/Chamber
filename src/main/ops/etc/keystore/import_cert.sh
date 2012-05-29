export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_32
$JAVA_HOME/bin/keytool -import -alias gd_gdroot_g2_cross -file ./godaddy_gdroot-g2_cross.crt -storepass Ech0ed1nc -trustcacerts -keystore Echoed20120224.keystore
$JAVA_HOME/bin/keytool -import -alias gd_gdroot_g2_mscvr_cross -file ./godaddy_mscvr-cross-gdroot-g2.crt -storepass Ech0ed1nc -trustcacerts -keystore Echoed20120224.keystore
$JAVA_HOME/bin/keytool -import -alias gd_gdroot_g2_root -file ./godaddy_gdroot-g2.crt -storepass Ech0ed1nc -trustcacerts -keystore Echoed20120224.keystore
$JAVA_HOME/bin/keytool -import -alias gd_class2_root -file ./godaddy_gd-class2-root.crt -storepass Ech0ed1nc -trustcacerts -keystore Echoed20120224.keystore
$JAVA_HOME/bin/keytool -import -alias gd_gdig2_ca -file ./godaddy_gdig2_ca.crt -storepass Ech0ed1nc -trustcacerts -keystore Echoed20120224.keystore


## godaddy_gd-class2-root.crt
### godaddy_gd_cross_intermediate.crt
## godaddy_gdig2_ca.crt
### godaddy_gd_intermediate.crt
# godaddy_gdroot-g2_cross.crt
# godaddy_gdroot-g2.crt
# godaddy_mscvr-cross-gdroot-g2.crt
