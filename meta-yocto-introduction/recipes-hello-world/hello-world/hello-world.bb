DESCRIPTION = "hello world"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://hello.c"

S = "${WORKDIR}"

do_compile() {
	${CC} hello.c ${LDFLAGS} -o sayhello
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 sayhello ${D}${bindir}
}