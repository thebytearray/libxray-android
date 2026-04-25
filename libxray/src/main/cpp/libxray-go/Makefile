BUILDDIR ?= $(CURDIR)/build
DESTDIR ?= $(CURDIR)/out

NDK_GO_ARCH_MAP_x86 := 386
NDK_GO_ARCH_MAP_x86_64 := amd64
NDK_GO_ARCH_MAP_arm := arm
NDK_GO_ARCH_MAP_arm64 := arm64
NDK_GO_ARCH_MAP_mips := mipsx
NDK_GO_ARCH_MAP_mips64 := mips64x

comma := ,
CLANG_FLAGS := --target=$(TARGET) --sysroot=$(SYSROOT)
export CGO_CFLAGS := $(CLANG_FLAGS) $(subst -mthumb,-marm,$(CFLAGS))
export CGO_LDFLAGS := $(CLANG_FLAGS) $(patsubst -Wl$(comma)--build-id=%,-Wl$(comma)--build-id=none,$(LDFLAGS)) -Wl,-soname=libxray-go.so
export GOARCH := $(NDK_GO_ARCH_MAP_$(ANDROID_ARCH_NAME))
export GOOS := android
export CGO_ENABLED := 1

default: $(DESTDIR)/libxray-go.so

$(DESTDIR)/libxray-go.so: export PATH := $(shell dirname $(CC)):$(PATH)
$(DESTDIR)/libxray-go.so: go.mod go.sum api-android.go jni.c protect_jni.c protect_jni.h
	@mkdir -p "$(dir $@)"
	go build -ldflags="-buildid=" -v -trimpath -buildvcs=false -o "$@" -buildmode c-shared

.DELETE_ON_ERROR:
