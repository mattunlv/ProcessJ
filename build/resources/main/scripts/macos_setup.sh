#!/bin/bash

# Clean up the terminal first
clear && reset

USER=$(logname)
GROUP="staff"
REPOSITORY="https://www.github.com/mattunlv/ProcessJ.git"

## -----------
## Directories

INSTALL_DIRECTORY="/opt/ProcessJ"
WORKING_DIRECTORY="/Users/$USER/workingpj"

## ----------
## File Paths

CONFIGURATION_PATH="/Users/$USER/processjrc"

COMPILER_PATH="$INSTALL_DIRECTORY/pjc"
EXECUTOR_PATH="$INSTALL_DIRECTORY/pj"

COMPILER_LINK_PATH="/usr/local/bin/pjc"
EXECUTOR_LINK_PATH="/usr/local/bin/pj"

## ----------------
## Directory Checks

# Just in case
if [ ! -d "/Users/" ]
then

    # Create and give ownership
    echo "Creating home directory for $USER"
    mkdir /Users/
    mkdir /Users/$USER
    chown -R $USER:$GROUP /Users/$USER
    chmod 755 /Users/$USER

fi

# Check install directory
if [ ! -d "/opt/" ]
then

    echo "Creating /opt"
    mkdir /opt

fi

# If there's a current version, remove it
if [ -d "$INSTALL_DIRECTORY" ]
then

    echo "Cleaning previous installation"
    rm -r $INSTALL_DIRECTORY

fi

# Create it
mkdir $INSTALL_DIRECTORY

# Check the working directory
if [ -d "$WORKING_DIRECTORY" ]
then

    echo "Removing $WORKING_DIRECTORY"
    rm -r $WORKING_DIRECTORY

fi

# Create & give ownership
echo "Creating working directory: $WORKING_DIRECTORY"
mkdir $WORKING_DIRECTORY
chown -R $USER:$GROUP $WORKING_DIRECTORY
chmod 755 $WORKING_DIRECTORY

## -----------
## File Checks

# Force removal
rm $CONFIGURATION_PATH > /dev/null 2>&1
rm $COMPILER_LINK_PATH > /dev/null 2>&1
rm $EXECUTOR_LINK_PATH > /dev/null 2>&1

echo "Creating configuration file for $USER"
touch $CONFIGURATION_PATH

# Set the license
echo "## Licensed to the Apache Software Foundation (ASF) under one or more"         >> $CONFIGURATION_PATH
echo "## contributor license agreements.  See the NOTICE file distributed with"      >> $CONFIGURATION_PATH
echo "## this work for additional information regarding copyright ownership."        >> $CONFIGURATION_PATH
echo "## The ASF licenses this file to You under the Apache Licence, Version 2.0"    >> $CONFIGURATION_PATH
echo "## (the "License"); you may not use this file except in compliance with"       >> $CONFIGURATION_PATH
echo "## the License. You may obtain a copy of the License at"                       >> $CONFIGURATION_PATH
echo "##"                                                                            >> $CONFIGURATION_PATH
echo "##    http://www.apache.org/licenses/LICENSE-2.0"                              >> $CONFIGURATION_PATH
echo "##"                                                                            >> $CONFIGURATION_PATH
echo "## Unless required by applicable law or agree to in writing, software"         >> $CONFIGURATION_PATH
echo "## distributed under the License is ditributed on an "AS IS" BASIS,"           >> $CONFIGURATION_PATH
echo "## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied." >> $CONFIGURATION_PATH
echo "## See the License for the specific language governing permissions and"        >> $CONFIGURATION_PATH
echo "## limitations under the License."                                             >> $CONFIGURATION_PATH
echo ""                                                                              >> $CONFIGURATION_PATH
echo "workingdir=workingpj"                                                          >> $CONFIGURATION_PATH
echo "installdir=$INSTALL_DIRECTORY"                                                 >> $CONFIGURATION_PATH

# Give ownership
chown -R $USER:$GROUP $CONFIGURATION_PATH
chmod 755 $CONFIGURATION_PATH

# Clone the repository into the install directory
echo "Retrieving from $REPOSITORY"
git clone $REPOSITORY $INSTALL_DIRECTORY > /dev/null 2>&1

# Create the Symbolic links
echo "Creating symbolic links"
ln -s $COMPILER_PATH $COMPILER_LINK_PATH
ln -s $EXECUTOR_PATH $EXECUTOR_LINK_PATH

# Build
cd $INSTALL_DIRECTORY
echo "Building...."
ant > /dev/null

# Give ownership to the user
chmod -R 777 $INSTALL_DIRECTORY
cd /Users/$USER
echo "Finished!"
