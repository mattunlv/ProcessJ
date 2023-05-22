#!/bin/bash

## Licensed to the Apache Software Foundation (ASF) under one or more
## contributor license agreements.  See the NOTICE file distributed with
## this work for additional information regarding copyright ownership.
## The ASF licenses this file to You under the Apache License, Version 2.0
## (the "License"); you may not use this file except in compliance with
## the License.  You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

##############################################################################
##                                                                          ##
##  ProcessJ Group - University of Nevada, Las Vegas                        ##
##                                                                          ##
##  use -classpath just as in java to use a custom classpath                ##
##                                                                          ##
##############################################################################

homedir=~
eval homedir=$homedir

## configuration file containing path and other resource information
CONFIG_FILE="processjrc"

## working directory for generated Java files
workingdir=""
## install directory for processj source files
installdir=""

############################################################################

## check if configuration file exists
if [ ! -e "$homedir/$CONFIG_FILE" ] ; then
  echo "Missing ProcessJ configuration file."
  echo "The configuration file should be in '$homedir'"
  exit 1
fi

## set configuration file
CONFIG_FILE="$homedir/$CONFIG_FILE"

############################################################################

## check if the processjrc file contains a working directory
if ! grep "workingdir" "$CONFIG_FILE" > /dev/null ; then
    echo "Missing 'workingdir' in '$CONFIG_FILE'"
    exit 1
else
    ## check if the working directory is specified in processjrc
    workingdir="`grep workingdir $CONFIG_FILE | cut -d '=' -f 2 | sed 's/ //g'`"
    if [ -z  "$workingdir" ]; then
        echo "Configuration file must contain a line of the form: 'workingdir=...'"
        exit 1
    fi
fi

## set working directory
workingdir="$homedir/$workingdir"

############################################################################

## check if the working directory exists
if [ ! -d "$workingdir" ] ; then
    echo "Missing working directory '$workingdir'"
    read -p "Do you wish to create '$workingdir'? [y/n] " yn
    case $yn in
        [Yy]* )
            mkdir $workingdir
            chmod 755 $workingdir
            echo "Working directory created";;
        [Nn]* )
            echo "Working directory not created"
            exit;;
        * )
            exit;;
    esac
fi

## clean working directory
rm -i -rf $workingdir/* > /dev/null

############################################################################

## check if the processjrc file contains an install directory
installdir="`grep installdir $CONFIG_FILE | cut -d '=' -f 2 | sed 's/ //g'`"

if [ -z "$installdir" ] ; then
    echo "Configuration file must contain a line of the form: 'installdir=...'"
    exit 1
fi

COMPILER="$installdir/pjc"
EXECUTOR="$installdir/pj"
TEST_DIRECTORY="$installdir/tests/"
HEADER_WIDTH=64

print_vertical_border() {

    index=0
    echo -n "//"

    while [ $index -lt $HEADER_WIDTH ]
    do

        index=$(($index+1))
        echo -n "="

    done

    echo "//"

}

print_title() {

    STRING=$1
    LENGTH=${#STRING}
    SPACE='_'

    fill=$((($HEADER_WIDTH - $LENGTH)/2))
    isOdd=$(($LENGTH%2))

    print_vertical_border

    echo -n "//"

    index=0

    while [ $index -lt $fill ]
    do

        index=$(($index+1))
        echo -n ${SPACE}

    done

    echo -n "$1"

    index=0

    while [ $index -lt $fill ]
    do

        index=$((index+1))
        echo -n ${SPACE}

    done

    if [ $isOdd == "1" ]
    then
        echo -n $SPACE
    fi

    echo "//"

    print_vertical_border

    echo ""

}

for processj_source in $(find $TEST_DIRECTORY -name '*.pj')
do

    NAME=${processj_source%%.*}
    NAME=${NAME#$TEST_DIRECTORY}

    print_title $NAME

    $COMPILER $processj_source > /dev/null 2&>1
    $EXECUTOR $NAME

    rm "$installdir/$NAME.jar" > /dev/null 2&>1

    echo ""

done

