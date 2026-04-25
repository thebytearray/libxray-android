cmake_minimum_required(VERSION 3.22.1)
project("libxray" LANGUAGES C CXX)

if(NOT DEFINED ANDROID_NDK)
    message(FATAL_ERROR "ANDROID_NDK is required to build libxray-go")
endif()

if(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(MAP_ARCH "arm")
elseif(ANDROID_ABI STREQUAL "arm64-v8a")
    set(MAP_ARCH "arm64")
elseif(ANDROID_ABI STREQUAL "x86")
    set(MAP_ARCH "x86")
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(MAP_ARCH "x86_64")
else()
    set(MAP_ARCH "arm64")
endif()

if(NOT DEFINED ANDROID_PACKAGE_NAME)
    set(ANDROID_PACKAGE_NAME "org.thebytearray.libxray.android")
endif()

find_program(MAKE_BIN NAMES make gmake REQUIRED)

get_filename_component(_ccdir "${CMAKE_C_COMPILER}" DIRECTORY)
if(WIN32)
    set(_pathsep "\\;")
else()
    set(_pathsep ":")
endif()
set(_path "${_ccdir}${_pathsep}$ENV{PATH}")
set(_dest "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")

add_custom_target(
    libxray-go.so
    WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/libxray-go"
    BYPRODUCTS "${_dest}/libxray-go.so"
    COMMAND ${CMAKE_COMMAND} -E env
    "PATH=${_path}"
    "ANDROID_ARCH_NAME=${MAP_ARCH}"
    "ANDROID_PACKAGE_NAME=${ANDROID_PACKAGE_NAME}"
    "CC=${CMAKE_C_COMPILER}"
    "CFLAGS=${CMAKE_C_FLAGS}"
    "LDFLAGS=${CMAKE_SHARED_LINKER_FLAGS}"
    "SYSROOT=${CMAKE_SYSROOT}"
    "TARGET=${CMAKE_C_COMPILER_TARGET}"
    "DESTDIR=${_dest}"
    "BUILDDIR=${_dest}/../generated-libxray-go"
    "GRADLE_USER_HOME=$ENV{GRADLE_USER_HOME}"
    ${MAKE_BIN}
    VERBATIM
)
