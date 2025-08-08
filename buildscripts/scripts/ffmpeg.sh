#!/bin/bash -e

. ../../include/path.sh

if [ "$1" == "build" ]; then
	true
elif [ "$1" == "clean" ]; then
	rm -rf _build$ndk_suffix
	exit 0
else
	exit 255
fi

mkdir -p _build$ndk_suffix
cd _build$ndk_suffix

cpu=armv7-a
[[ "$ndk_triple" == "aarch64"* ]] && cpu=armv8-a
[[ "$ndk_triple" == "x86_64"* ]] && cpu=generic
[[ "$ndk_triple" == "i686"* ]] && cpu="i686 --disable-asm"

cpuflags=
[[ "$ndk_triple" == "arm"* ]] && cpuflags="$cpuflags -mfpu=neon -mcpu=cortex-a8"

args=(
	--target-os=android --enable-cross-compile
	--cross-prefix=$ndk_triple- --cc=$CC --pkg-config=pkg-config --nm=llvm-nm
	--arch=${ndk_triple%%-*} --cpu=$cpu
	--extra-cflags="-I$prefix_dir/include $cpuflags" --extra-ldflags="-L$prefix_dir/lib"

	--enable-{jni,mediacodec,mbedtls,libdav1d} --disable-vulkan
	--disable-static --enable-shared --enable-{gpl,version3}

	# disable unneeded parts
	--disable-{stripping,doc,programs}
	# to keep the build lean we disable some feature quite aggressively:
	# - muxers, encoders: mpv-android does not have any way to use these
	# - devices: no practical use on Android
	--disable-{muxers,encoders,devices}
	# useful to taking screenshots
#	--enable-encoder=mjpeg,png
	# useful for the `dump-cache` command
#	--enable-muxer=mov,matroska,mpegts

	# 禁用所有已知的视频解码器
#  --disable-decoder=h264
#  --disable-decoder=hevc
#  --disable-decoder=vp8
#  --disable-decoder=vp9
#  --disable-decoder=av1
#  --disable-decoder=mpeg4
#  --disable-decoder=mpeg2video
#  --disable-decoder=mjpeg
#  --disable-decoder=theora
#  --disable-decoder=vc1
#  --disable-decoder=wmv3
#
#  # 禁用所有已知的视频解析器
#  --disable-parser=h264
#  --disable-parser=hevc
#  --disable-parser=mpeg4video
#  --disable-parser=mpeg2video
#  --disable-parser=vp8
#  --disable-parser=vp9
#  --disable-parser=av1
#  --disable-parser=vc1
#
#  # 禁用纯视频的解复用器，保留对mkv/mov等音频容器的支持
#  --disable-demuxer=mpegts
#  --disable-demuxer=avi
#  --disable-demuxer=flv
#  --disable-demuxer=hls
#
#  # 禁用流媒体协议
#  --disable-protocol=hls
#  --disable-protocol=pipe
#  --disable-protocol=rtmp*
#  --disable-protocol=rtmp
#  --disable-protocol=rtmpt
#  --disable-protocol=rtp
#  --disable-protocol=udp
#  --disable-protocol=mmsh
#  --disable-protocol=mmst
#
#  --disable-swscale
	--disable-filters
)
../configure "${args[@]}"

make -j$cores
make DESTDIR="$prefix_dir" install
