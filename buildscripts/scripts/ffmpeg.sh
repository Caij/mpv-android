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
	--enable-encoder=mjpeg,png
	# useful for the `dump-cache` command
	--enable-muxer=mov,matroska,mpegts

  # 禁用流媒体协议
  --disable-protocol=hls
  --disable-protocol=pipe
  --disable-protocol=rtmp*
  --disable-protocol=rtmp
  --disable-protocol=rtmpt
  --disable-protocol=rtp
  --disable-protocol=udp
  --disable-protocol=mmsh
  --disable-protocol=mmst

  --disable-decoders
  --enable-decoder=aac*
  --enable-decoder=ac3*
  --enable-decoder=adpcm*
  --enable-decoder=alac*
  --enable-decoder=amr*
  --enable-decoder=ape
  --enable-decoder=cook
  --enable-decoder=dca
  --enable-decoder=dolby_e
  --enable-decoder=eac3*
  --enable-decoder=flac
  --enable-decoder=mp1*
  --enable-decoder=mp2*
  --enable-decoder=mp3*
  --enable-decoder=opus
  --enable-decoder=pcm*
  --enable-decoder=sonic
  --enable-decoder=truehd
  --enable-decoder=tta
  --enable-decoder=vorbis
  --enable-decoder=wma*
  --enable-decoder=dsd*
  --enable-decoder=dca_ma

  --disable-demuxers
  --enable-demuxer=aac
  --enable-demuxer=ac3
  --enable-demuxer=aiff
  --enable-demuxer=amr
  --enable-demuxer=ape
  --enable-demuxer=asf
  --enable-demuxer=ass
  --enable-demuxer=caf
  --enable-demuxer=eac3
  --enable-demuxer=flac
  --enable-demuxer=loas
  --enable-demuxer=matroska
  --enable-demuxer=mov
  --enable-demuxer=mp3
  --enable-demuxer=mpeg*
  --enable-demuxer=ogg
  --enable-demuxer=srt
  --enable-demuxer=vc1
  --enable-demuxer=wav
  --enable-demuxer=dsf
  --enable-demuxer=dff
  --enable-demuxer=aiff
  --enable-demuxer=dts

  # 禁用所有已知的视频解析器
  --disable-parser=h264
  --disable-parser=hevc
  --disable-parser=mpeg4video
  --disable-parser=mpeg2video
  --disable-parser=vp8
  --disable-parser=vp9
  --disable-parser=av1
  --disable-parser=vc1

  --disable-filters
  --enable-filter=aformat
  --enable-filter=amix
  --enable-filter=anull
  --enable-filter=aresample
  --enable-filter=areverse
  --enable-filter=asetrate
  --enable-filter=atempo
  --enable-filter=atrim
  --enable-filter=equalizer
  --enable-filter=firequalizer
  --enable-filter=pan
  --enable-filter=superequalizer
  --enable-filter=volume
)
../configure "${args[@]}"

make -j$cores
make DESTDIR="$prefix_dir" install
