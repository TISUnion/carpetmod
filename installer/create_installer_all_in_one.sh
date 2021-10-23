#!/bin/bash

# For debug: Print commands and their arguments as they are executed.
#set -x

# Some colors for bash
RED='\033[0;31m'
NOCOLOR='\033[0m'

if [[ ! -f  ../build/distributions/carpetmod_1.13.2_Client.zip ]]; then
    echo "${RED}Patches missing, create them with /gradlew createRelease"
    exit 1
fi
if [[ ! -f ../build/distributions/carpetmod_1.13.2_Server.zip ]]; then
    echo "${RED}Patches missing, create them with /gradlew createRelease"
    exit 1
fi

[[ -d output ]] && rm -rf output
mkdir output

echo "${NOCOLOR}Copying files ..."
cp ../build/distributions/carpetmod_1.13.2_Client.zip output
cp ../build/distributions/carpetmod_1.13.2_Server.zip output
cp 7za.exe output
cp win_install_server.cmd output
cp win_install_singleplayer.cmd output
cp unix_install_server.sh output
cp unix_install_singleplayer.sh output
cp README.txt output
cp 1.13.2-carpet.json output

echo "Zipping ..."
[[ -f TISCarpet_installer.zip ]] && rm -f TISCarpet_installer.zip
pushd output > /dev/null
tar czf ../TISCarpet_installer.tar.gz *
popd > /dev/null

echo "Cleaning ..."
rm -rf output
