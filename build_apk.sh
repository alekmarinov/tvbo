#!/bin/sh
######################################################################
##
## Copyright (C) 2003-2015, Intelibo Ltd
##
## Project:       TVBO
## Filename:      build_apk.sh
## Description:   Build apk with ant
## Arguments:     <app name> <target apk directory>
##
######################################################################

# locate root directories
root_dir()
{
	local ROOT_DIR=$(readlink -f "$0")
	while [ ! -f "$ROOT_DIR/build_apk.sh" ]; do
		ROOT_DIR=$(dirname $ROOT_DIR)
	done
	echo $ROOT_DIR
}

# returns application version description
# param 1: application git repository
# param 2: version suffix, e.g. build number
app_version()
{
	local git_repo=$1
	local git_ver=$(cd $git_repo && git describe --match "[0-9]*")
	local normal_ver=${git_ver/\-/.}
	local app_ver=${normal_ver/%-*/}
	echo $app_ver
}

# return current base script name
script_name()
{
	local name=$(readlink -f $0)
	name=$(basename $name 2>/dev/null)
	echo $name
} 

# show usage info and exit with failure
usage()
{
	echo "Usage: $(script_name) $1"
	exit 1
}

# process script arguments
APP_NAME=$1 
TARGET_DIR=$2 
[[ -z $APP_NAME || -z $TARGET_DIR ]] && usage "<app name> <target directory>"

TARGET_DIR_DEBUG=$TARGET_DIR/debug
TARGET_DIR_RELEASE=$TARGET_DIR/unsigned

mkdir -p "$TARGET_DIR_DEBUG" "$TARGET_DIR_RELEASE"

ROOT_DIR=$(root_dir)

# generate sdk version
SDK_DIR=$ROOT_DIR/../tvbosdk
SDK_VERSION=$(app_version $SDK_DIR)
arrVer=(${SDK_VERSION//\./ })
SDK_VERSION_MAJOR=${arrVer[0]}
SDK_VERSION_MINOR=${arrVer[1]}
SDK_VERSION_REVISION=${arrVer[2]}
SDK_VERSION_REVISION=${SDK_VERSION_REVISION:-0}
SDK_VERSION=$SDK_VERSION_MAJOR.$SDK_VERSION_MINOR.$SDK_VERSION_REVISION

echo "Building TVBOSDK $SDK_VERSION in $SDK_DIR"

cd $SDK_DIR
ant clean
ant debug
exit_status=$?
git reset --hard
if [ $exit_status != 0 ]; then
	exit $exit_status
fi

# generate apk version
VERSION=$(app_version $ROOT_DIR)

cd $ROOT_DIR
echo "Building DEBUG $APP_NAME $VERSION in $ROOT_DIR"
sed -i "s/android:versionName=\".*\"/android:versionName=\"$VERSION\"/" AndroidManifest.xml
ant clean
ant debug
exit_status=$?
git reset --hard
if [ $exit_status != 0 ]; then
	exit $exit_status
fi
cp -v $ROOT_DIR/bin/$APP_NAME-debug.apk $TARGET_DIR_DEBUG/$APP_NAME-$VERSION.apk

echo "Building RELEASE $APP_NAME $VERSION in $ROOT_DIR"
sed -i "s/android:versionName=\".*\"/android:versionName=\"$VERSION\"/" AndroidManifest.xml
ant clean
ant release
exit_status=$?
git reset --hard
if [ $exit_status != 0 ]; then
	exit $exit_status
fi
cp -v $ROOT_DIR/bin/$APP_NAME-release-unsigned.apk $TARGET_DIR_RELEASE/$APP_NAME-$VERSION-unsigned.apk
