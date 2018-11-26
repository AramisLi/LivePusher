#!/usr/bin/env bash
#x264交叉编译环境-Linux

PREFIX=./android/armeabi-v7a
TOOLCHAIN=$NDK_DIR/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
FLAGS="-isysroot $NDK_DIR/sysroot -isystem $NDK_DIR/sysroot/usr/include/arm-linux-androideabi -isystem $NDK_DIR/sources/cxx-stl/llvm-libc++/include -isystem $NDK_DIR/sources/android/support/include -isystem $NDK_DIR/sources/cxx-stl/llvm-libc++abi/include -D__ANDROID_API__=17 -g -DANDROID -ffunction-sections -funwind-tables -fstack-protector-strong -no-canonical-prefixes -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -Wa,--noexecstack -Wformat -Werror=format-security  -O0 -fPIC"

./configure \
--prefix=$PREFIX \
--disable-cli \
--enable-static \
--enable-pic \
--host=arm-linux \
--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
--sysroot=$NDK_DIR/platforms/android-17/arch-arm \
--extra-cflags="$FLAGS"



make clean
make install