sudo systemctl stop octavian-converter.service
file="/opt/octavian-converter/octavian-converter.jar"
if [ -f "$file" ] ; then
    rm "$file"
fi
cp target/octavian-converter-*.jar /opt/octavian-converter/octavian-converter.jar
sudo systemctl start octavian-converter.service