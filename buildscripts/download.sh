#!/bin/bash -e

./include/download-sdk.sh
./include/download-deps.sh

#sed -i "s/dependency('libswscale', version: '>= 7.5.100')/dependency('libswscale', version: '>= 7.5.100', required: false)/" deps/mpv/meson.build
#cat deps/mpv/meson.build
