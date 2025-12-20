#! /bin/bash

debfile="$1"
workdir="./tmp_work/"

postinst_cmd1='xdg-desktop-menu install /opt/undereat-material/lib/undereat-material-undereat_material.desktop'
postinst_cmd2='xdg-mime install --novendor /opt/undereat-material/lib/app/resources/undereat_material.xml'

prerm_cmd1='xdg-desktop-menu uninstall /opt/undereat-material/lib/undereat-material-undereat_material.desktop'
prerm_cmd2='xdg-mime uninstall /opt/undereat-material/lib/app/resources/undereat_material.xml'

mkdir -p "$workdir"
cp -av "$debfile" "$workdir"

cd "$workdir" || exit 1

ls -al
ar -x "$debfile"
ls -al

# --use-compress-program=unzstd if fileending is tar.zst

tar -xvf control.tar.xz

sed -i -e 's#^xdg-desktop-menu.*$#'"$postinst_cmd1"'\n'"$postinst_cmd2"'#g' postinst
sed -i -e 's#^xdg-desktop-menu.*$#'"$prerm_cmd1"'\n'"$prerm_cmd2"'#g' prerm

rm -f control.tar.xz
tar --owner 0 --group 0 -cJvf control.tar.xz control postinst postrm preinst prerm

rm -f control postinst postrm preinst prerm

mkdir -p d_/
cd d_/ || exit 1
tar -xvf ../data.tar.xz

desktop_file="./opt/undereat-material/lib/undereat-material-undereat_material.desktop"

cat "$desktop_file"

sed -i -e 's#Exec=/opt/undereat-material/bin/undereat_material#Exec=/opt/undereat-material/bin/undereat_material %U#' "$desktop_file"
sed -i -e 's#Comment=.*$#Comment=Undereat#' "$desktop_file"
sed -i -e 's#Name=.*$#Name=Undereat Material#' "$desktop_file"
sed -i -e 's#MimeType=.*$#MimeType=application/x-undereat-db#' "$desktop_file"
sed -i -e 's#Categories=.*$#Categories=Utility#' "$desktop_file"

echo 'StartupWMClass=UndereatMainKt' >> "$desktop_file"

cat "$desktop_file"

xz --decompress ../data.tar.xz

tar --delete -vf ../data.tar "$desktop_file"

echo "checking ..."
tar -tvf ../data.tar | grep '\.desktop'
echo "checking ... DONE"

tar --owner 0 --group 0 -rvf ../data.tar "$desktop_file"

echo "checking ..."
tar -tvf ../data.tar | grep '\.desktop'
echo "checking ... DONE"

xz --compress ../data.tar

ls -al ../

cd ../ && rm -Rf d_/

ar rc final_pkg.deb debian-binary control.tar.xz data.tar.xz
cp -av final_pkg.deb ../ || exit 1

cd ../ && rm -Rf "$workdir"


