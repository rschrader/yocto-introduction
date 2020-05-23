# Install Docker
## Linux

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
# Setup VSCode for remote development

Install [VSCode](https://code.visualstudio.com/). Please use the precompiled binary version and not the OSS version. Otherwise the remote development will not work properly.

Install [Remote Development Extensionpack](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.vscode-remote-extensionpack) for your VSCode installation. 



