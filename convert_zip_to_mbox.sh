#!/bin/sh

SYMPA2MBOX=sympa2mbox.sh
FOLDER=$1

if [ ! -d "$1" ]
then
  echo "Usage: $0 folder"
  exit 0
fi

if [ ! -r "$SYMPA2MBOX" ]
then
  echo "Missing script in the same folder: $SYMPA2MBOX"
  exit 0
fi

function convert() {
  echo `basename $i .zip`
  rm -rf temp
  mkdir temp
  pushd temp
  unzip ../$i
  popd
  ../$SYMPA2MBOX "temp" "$(basename $i .zip).mbox"
  rm -rf temp
  rm -f $i
}

cd $FOLDER
find . -name "*.zip" | while read i
do
  convert $i
done
