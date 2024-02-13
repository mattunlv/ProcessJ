# The ProcessJ Language

A new programming language being developed at the University of Nevada, Las Vegas.

## The easiest way to get started

```bash
curl https://raw.githubusercontent.com/mattunlv/ProcessJ/main/setup.sh >> setup.sh && chmod a+x setup.sh && ./setup.sh
```

Run the command above on your terminal.

## Setup

This section outlines the required steps to get the ProcessJ compiler functional on your system.
In order to facilitate a hassle-free installation, a setup script for:

- [Linux](https://github.com/mattunlv/ProcessJ/blob/main/setup.sh)
- [MacOS](https://github.com/mattunlv/ProcessJ/blob/main/macos_setup.sh)

are included; simply download the pertinent script (not the entire repository) and execute with sudo (or su);

The setup script will also perform clean-up of any older versions of the ProcessJ compiler and give the
appropriate permissions in order to allow for standard execution, as well as execute the steps outlined
below.

More specifically, the script will:

- Place the ProcessJ files in /opt
- Create a working directory in the user's home directory
- Create symbolic links to the compiler and executor
- Generate a corresponding configuration file

Otherwise if a custom setup is preferred, follow & adjust according to the steps specified below.

At the time of writing, this installer has been tested on:

- x86-64, 64-Bit CentOS
- x86-64, 64-Bit Debian 11 (bullseye)
- x86-64, 64-Bit Ubuntu 22.04 (jammy)
- x86-64, 64-Bit Arch 2022.08.05
- x86-64, 64-Bit Fedora Cloud Base 34
- x86-64, 64-Bit Red-Hat Enterprise Linux
- ARM64,  64-Bit Macbook Pro M1


### Pre-Requisites

The following dependencies are required prior to installation:

- Java Development Kit (JDK; default-jdk on Debian-based systems)
- ant
- make
- git

#### Installation of dependencies on Debian-based systems

**Debian-based systems:**

```bash
$ sudo apt-get install -y default-jdk ant make git
```

Otherwise if sudo is not configured on your system run:

```bash
$ su
$ apt-get install -y default-jdk ant make git
$ exit
```

**Arch:**

```bash
$ sudo pacman -S jdk-openjdk git make ant
```

**Fedora:**

```bash
$ sudo dnf git-all java-latest-openjdk.devel make ant
```

**CentOS & RHEL:**

```bash
$ sudo yum install java git make ant
```

**MacOS systems:**

In order to install the required dependencies on MacOS, homebrew must be installed from [here](https://brew.sh).
(Copy & paste the command in a terminal).

```bash
$ brew install -y openjdk git ant make
```

### Clone the repository

```bash
$ git clone https://www.github.com/mattunlv/ProcessJ.git
```
This will clone the repository into the current working directory. If you wish to place the folder elsewhere,
a path to the desired directory can be specified, but must exist before cloning.

If the folder exists:

```bash
$ git clone https://www.github.com/mattunlv/ProcessJ.git /path/to/installation/folder
```

Otherwise:

```bash
$ mkdir /path/to/installation/folder && git clone https://www.github.com/mattunlv/ProcessJ.git /path/to/installation/folder
```

#### Build the compiler

The compiler must be built prior to assigning user permissions & execution:

```bash
$ cd /path/to/installation/folder
$ ant
```

The compiler must build successfully. If there are any problems please contact any or all of the following:

- matt.pedersen@unlv.edu (Dr. Pedersen)
- cisneo1@unlv.nevada.edu (Ben Cisneros)
- carlos.cuenca@unlv.edu (Carlos Cuenca)

If placing the files into a system directory (root-owned), read/write/execute permissions for all subfolders & files
must be granted:

**Arch, CentOS, Debian, Fedora, RHEL, & Ubuntu:**

```bash
$ sudo chown -R $(logname):$(logname) /path/to/installation/folder
$ sudo chmod -R 755 /path/to/installation/folder
```

Alternatively, if sudo is not configured on your system (Arch, CentOS, Debian, Fedora, RHEL, & Ubuntu):

```bash
$ su
$ chown -R $(logname):$(logname) /path/to/installation/folder
$ chmod 755 /path/to/installation/folder
$ exit
```

**MacOS systems:**

```bash
$ sudo chown -R $(logname):staff /path/to/installation/folder
$ sudo chmod 755 /path/to/installation/folder
```

The included setup script performs the clone, places the contents into /opt/, & gives the corresponding permissions automatically.

### Working Directory

The ProcessJ compiler uses a working directory to place the generated code. Furthermore, the working directory 
must be placed in the user's home folder and be titled "workingpj"

**Arch, CentOS, Debian, Fedora, RHEL, & Ubuntu:**

```bash
$ mkdir /home/$(logname)/workingpj
```

**MacOS systems:**

```bash
$ mkdir /Users/$(logname)/workingpj
```

The included setup script generates this folder automatically

### Configuration File

A valid configuration file must be placed and titled "processjrc" in the user's home directory:

**Arch, CentOS, Debian, Fedora, RHEL, & Ubuntu:**

```bash
$ touch /home/$(logname)/processjrc
```

**MacOS systems:**

```bash
$ touch /Users/$(logname)/processjrc
```

and must include the following two lines:

```bash
workingdir=workingpj
installdir=/path/to/installation/folder
```

The included setup script generates this file automatically.

### Symbolic Links

Symbolic links that point to the compiler and executor can be created in order to compile from any directory:

**Arch, CentOS, Debian, Fedora, RHEL, & Ubuntu:**

```bash
$ ln -s /path/to/installation/folder/pjc /usr/bin/pjc
$ ln -s /path/to/installation/folder/pj  /usr/bin/pj
```

**MacOS systems:**

```bash
$ ln -s /path/to/installation/folder/pjc /usr/local/bin/pjc
$ ln -s /path/to/installation/folder/pj  /usr/local/bin/pj
```

The included setup script creates the symbolic links automatically

## Compilation

In order to compile programs written in ProcessJ, execute the following command (if a symbolic link was created):

```bash
$ pjc /path/to/processj/source/file
```

Otherwise, navigate to the ProcessJ directory and enter:

```bash
$ ./pjc /path/to/processj/source/file
```

## Execution

By default, the ProcessJ compiler outputs a `.jar` file in the current working directory (the directory in which the user executed the compilation).
In order to execute the compiled program, the `.jar` file extension must be omitted from the command.

For example (if symbolic links were created):

```bash
$ pjc coolprogram.pj
$ pj coolprogram
```

Otherwise, navigate to the ProcessJ directory and enter:

```bash
$ ./pjc /path/to/source/coolprogram.pj
$ ./pj coolprogram
```
