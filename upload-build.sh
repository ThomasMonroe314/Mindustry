#!/usr/bin/env bash

# cd $HOME
git config --global user.email $GHEMAIL
git config --global user.name $GHUSERNAME

# add, commit and push files
git clone https://github.com/Anuken/Mindustry.wiki.git
cd Mindustry.wiki

DESKFILE=$TRAVIS_BUILD_NUMBER"-desktop-bleeding-edge.jar"
cp ../desktop/build/libs/desktop-release.jar $DESKFILE

FILE1="Bleeding-Edge-Build-$TRAVIS_BUILD_NUMBER.md"

if [ ! -e $FILE1 ]; then
    touch $FILE1
fi

NEWLINE=$'\n'
echo "### Commit #"$TRAVIS_COMMIT".${NEWLINE}Desktop JAR download: [Link]("$DESKFILE")" >> $FILE1

git add $FILE1
git add $DESKFILE
git commit -m "Added a new bleeding edge build"

# now remove old build

OLD_TRAVIS_BUILD_NUMBER=`expr $TRAVIS_BUILD_NUMBER - 7`
OLD_DESKFILE=$OLD_TRAVIS_BUILD_BUMBER"-desktop-bleeding-edge.jar"
OLD_FILE1="Bleeding-Edge-Build-"$OLD_TRAVIS_BUILD_NUMBER".md"

if [ -e $OLD_FILE1 ]; then
    rm -f $OLD_FILE1
    rm -f $OLD_DESKFILE
    git add $OLD_FILE1
    git add $OLD_DESKFILE
fi

git push https://$GHUSERNAME:$GHPASSWORD@github.com/Anuken/Mindustry.wiki.git --all
