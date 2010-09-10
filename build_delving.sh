#!/usr/bin/env sh

# This is the maven install script for Delving

INSTALL="mvn clean install -Dmaven.test.skip=true"
PACKAGE="mvn clean package -Dmaven.test.skip=true"
BUILD_ALL=false

# Installation of jar is m2 repository
cd core; $INSTALL
cd ../sip-core; $INSTALL

# Packaging of War files
cd ../portal; $PACKAGE
if [[ BUILD_ALL ]]; then
	#statements
	echo "building extra modules"
	cd ../services; $PACKAGE
	cd ../sip-creator; $PACKAGE
fi
