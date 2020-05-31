# Setup the project

Checkout the repository and update the submodule to also checkout the git submodules used in this example project. The submodules used are `/poky` and `/meta-raspberrpi`

```
git clone https://github.com/rschrader/yocto-introduction.git  
git submodule update --init
```

# Open project in docker environment
Its recommended to use the docker environment that is already prepared with all necessary dependencies to build your yocto-image. This project is prepared to be run with VSCode and the Remote Development Extension.

## Setup VSCode and Remote Development Extension
### Install Docker

Follow the guide on [https://docs.docker.com/engine/install/](https://docs.docker.com/engine/install/) depending on the distribution you are using.

Start docker server:
```
sudo systemctl start docker
```

Add yourself to the dockergroup:
(dont forget you have to login again to refresh groups properly)
```
sudo usermod -a -G docker <your_user>
```
### Setup VSCode for remote development

Install [VSCode](https://code.visualstudio.com/). Please use the precompiled binary version and not the OSS version. Otherwise the remote development will not work properly.

Install [Remote Development Extensionpack](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.vscode-remote-extensionpack) for your VSCode installation. 

### Start in container
Open your project in visual studio code and reopen it in a container: In vscode open the command palette: Ctrl + Shift + P and run `Remote-Containers: Reopen in Container`. This will open your project in the container while running VSCode on your hostmachine. All commands you now enter on the terminal inside of VSCode is executed in the docker environment.

# Create a first image
After you setup your buikd environment we can start with building the first image from the poky distribution and start in a QEMU Emultaion. Since poky is already added as a git submodule to the project it is already downloaded to the project folder.

## Build Core Image Native
To build the image run these commands inside of your docker container. This can take several hours to finish on the first run. When running again a lot of cached results from previous builds are used and the build will finish a lot faster.

```
source poky/oe-init-build-env
bitbake core-image-minimal
```

## Run your image in in qemu
To run the image in qemu the open embedded build environment provides a script called `runqemu`, which makes use of the qemu configurations that are deployed together with your built image.

To allow running the image inside of the docker container the additional parameters `slirp` and `nogrpahic` are necessary. For more information on the runqemu options see [8.10. runqemu Command-Line Options](https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#qemu-dev-runqemu-command-line-options)

Start qemu with the following comman in your sourced build environment

```
runqemu qemux86-64 slirp nographic
```

Login in with user `root` and no password. 

You can exit qemu by shut down the virtual machine:
```
shutdown -h now
```

# Create a custom package
In this example project the custom package is already added in the meta-layer meta-yocto-introduction. The added meta-layers are added to the project using the `build/conf/bblayers.conf` and this file is part of your local build environment created by the OE Buildsystem. Therfore it is still necessary to add the layer to the config using the `bitbake-layers add-layer ../meta-yocto-introduction` command.

The steps to create a custom package are still described below, so you can try to add your own application as well.

## Add a custom meta layer
Since meta layers are used to organize recipes. And recipes provide the description how to build a package
we first have to a add a new meta-layer for the recipe. After that the meta-layer is added to the project.

```
bitbake-layers create-layer ../meta-yocto-introduction
bitbake-layers add-layer ../meta-yocto-introduction
```

## Create recipe

Extend the created meta layer with the `recipes-hello-world/hello-world/files` folder for your new application. 

```
mkdir -p meta-yocto-introduction/recipes-hello-world/hello-world/files
touch meta-yocto-introduction/recipes-hello-world/hello-world/files/hello.c
touch meta-yocto-introduction/recipes-hello-world/hello-world/hello-world.bb
```

The result should look like this:

```
meta-yocto-introduction
├── conf
│   └── layer.conf
├── COPYING.MIT
├── README
├── recipes-example
└── recipes-hello-world
    └── hello-world
        ├── files
        │   └── hello.c
        └── hello-world.bb

```
### hello.c
Add the source code for your application. In this example we are placing the source code directly in the yocto project folder. Normally the files are loaded from an external repository.

```
#include <stdio.h>

int main() {
	printf("Hello, World!\n");
	return 0;
}
```

### hello<span>-world.bb
This is the recipe which describes the steps to build the hello-world application as package and make availale to be installed in an image.

```
DESCRIPTION = "hello world"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://myhello.c"

S = "${WORKDIR}"

do_compile() {
	${CC} hello.c ${LDFLAGS} -o sayhello
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 sayhello ${D}${bindir}
}
```

## Add recipe to the image

To make the application available in the image the name of the package has to be appended to the `IMAGE_INSTALL` variable. This can be done in any configuration file that is parsed when building your image or in the image recipe itself. I recommend to add it to the layer.conf of the new meta-layer. In this case the package is added whenever the meta layer is loaded by the bblayers.conf

```
echo 'IMAGE_INSTALL_append = "hello-world"' >> meta-yocto-introduction/conf/layer.conf
```

## Check result in QEMU
start new image in qemu
```
runqemu qemux86-64 slirp nographic
```

login and execute your new application:
```
qemux86-64 login: root
root@qemux86-64:~# sayhello 
Hello, World!
```

# Build target for the RaspberryPi



## Add BSP-Layer
Part of the example project is also the git submodule `meta-raspberrypi`. This meta layer is the bsp for the raspberry pi and makes the raspberry pi machines available in the project. The submodules references the `zeus` branch of `git://git.yoctoproject.org/meta-raspberrypi`. This branch matches the used poky version.

Add the meta-raspberrypi layer to the project
```
bitbake-layers add-layer ../meta-raspberrypi
```

## Build image
Now you can build the image for the raspberrypi, we have ran before on qemu.
```
MACHINE="raspberrypi4" bitbake core-image-minimal
```

## Prepare SD-Card

Create two partitions:
 - boot partition: fat32, 100Mb
 - root partition: ext4, remaining storage (at least 1GB)

On Linux you can use the following commands after you figured out which sd-device your sdcard and you unmounted it:
```
parted -s /dev/<sd-device> \\nmklabel msdos \\nmkpart primary fat32 1M 100M \\nmkpart primary ext4 100M 100%
mkfs.vfat /dev/<sd-device>1
mkfs.ext4 /dev/<sd-device>2
fatlabel /dev/<sd-device>1 BOOT
e2label /dev/<sd-device>2 ROOT
```

## Copy Files to SD-Card

change to your build results: 

```
cd <sample-project>/build/tmp/deploy/
```

### Boot Partition

- Bootloader `bcm2835-bootfiles/*`
- Kernel `zImage` (rename to: `kernel.img`)
- Device Tree `bcm2711-rpi-4-b.dtb`

```
cp bcm2835-bootfiles/* /run/media/raphael/BOOT/
cp zImage /run/media/raphael/BOOT/kernel.img
cp bcm2711-rpi-4-b.dtb /run/media/raphael/BOOT
```

### Root Partition
Extract compressed rootfs `core-image-minimal-raspberrypi4.tar.bz2` to root partition

```
tar -xjf ./core-image-minimal-raspberrypi4.tar.bz2 -C /run/media/raphael/ROOT
```

## Boot the RaspberryPI

you should see the following output after a successful boot:

```
...

Poky (Yocto Project Reference Distro) 3.0.3 raspberrypi4 /dev/tty1

raspberrypi4 login: root
root@raspberrypi4:~# sayhello
Hello, World
```