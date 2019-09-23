# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

set(libs_DIR libs/${ANDROID_ABI})
set(libs_include_DIR src/main/cpp)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)



include_directories(${libs_include_DIR})

link_directories(${libs_DIR})

add_library( # Sets the name of the library.
        open_basex_lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/open_basex.cpp
        src/main/cpp/sai_micbasex_interface.h)



# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        open_basex_lib sai_micbasex

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

add_definitions(-std=c++11)





