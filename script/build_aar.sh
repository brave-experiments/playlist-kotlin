#
# Copyright (c) 2023 The Brave Authors. All rights reserved.
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this file,
# You can obtain one at https://mozilla.org/MPL/2.0/.
#

sudo apt-get --quiet update --yes
sudo apt-get --quiet install --yes wget tar unzip lib32stdc++6 lib32z1

# Create a new directory at specified location
sudo rm -rf SDK
mkdir SDK
cd SDK

# Create a new directory android-sdk-latest
# Next steps are necessary to remove "SDK location not found" error
mkdir android-sdk-latest
cd android-sdk-latest

export ANDROID_SDK_HOME=${PWD}
echo "ANDROID_SDK_HOME : " $ANDROID_SDK_HOME
export PATH=$PATH:"$ANDROID_SDK_HOME/tools:$ANDROID_SDK_HOME/platform-tools"
export PATH="/usr/bin:$PATH"

mkdir cmdline-tools
cd cmdline-tools

# Here we are installing androidSDK tools from official source,
# after that unzipping those tools and
# then running a series of SDK manager commands to install necessary android SDK packages that’ll allow the app to build
wget https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip
unzip commandlinetools-linux-7583922_latest.zip

mv cmdline-tools latest
cd latest/bin

yes | $ANDROID_SDK_HOME/cmdline-tools/latest/bin/sdkmanager --licenses || true
sh sdkmanager "build-tools;30.0.3"

cd ../../../../..

# Clone and build app
sudo rm -rf playlist-kotlin/
git clone https://github.com/brave-experiments/playlist-kotlin.git
cd playlist-kotlin
echo "sdk.dir=$ANDROID_SDK_HOME" > local.properties

sudo chmod +x ./gradlew
sudo ./gradlew playlist-kotlin:assembleRelease
