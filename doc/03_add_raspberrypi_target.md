```
cd sample-project
git submodule add -b zeus git://git.yoctoproject.org/meta-raspberrypi
cd build
bitbake-layers add-layer ../meta-raspberrypi
```


```
MACHINE="raspberrypi4" bitbake core-image-minimal
```