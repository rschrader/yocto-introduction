FROM fedora:31

# install required packages according to yocto docs: https://www.yoctoproject.org/docs/3.1/ref-manual/ref-manual.html#required-packages-for-the-build-host 
RUN sudo dnf install -yq gawk make wget tar bzip2 gzip python3 unzip perl patch \
    diffutils diffstat git cpp gcc gcc-c++ glibc-devel texinfo chrpath \
    ccache perl-Data-Dumper perl-Text-ParseWords perl-Thread-Queue perl-bignum socat \
    python3-pexpect findutils which file cpio python python3-pip xz python3-GitPython \
    python3-jinja2 SDL-devel xterm rpcgen python2

# additional    
RUN sudo dnf install -yq hostname glibc-locale-source glibc-langpack-en

# configure locale 
ENV LANG=en_US.utf8
RUN localedef -c -i en_US -f UTF-8 en_US.UTF-8


# add yoctobuilde user
RUN useradd -ms /bin/bash yoctobuilder
RUN usermod -aG wheel yoctobuilder
RUN echo "yoctobuilder:yoctobuilder" | chpasswd
USER yoctobuilder
WORKDIR /home/yoctobuilder
