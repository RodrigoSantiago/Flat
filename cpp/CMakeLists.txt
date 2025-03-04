cmake_minimum_required(VERSION 3.6)
project(Flat)

find_package(Java REQUIRED)
find_package(JNI REQUIRED)
include(UseJava)
set(CMAKE_CXX_STANDARD 11)

include_directories(include
        src/gl
        src/ml
        src/wl
        src/svg
        src/flatvectors
        ${JNI_INCLUDE_DIRS})

link_directories(lib)

set(SOURCE_FILES
        src/glad.c
        src/wl/flat_backend_WL.cpp
        src/gl/flat_backend_GL.cpp
        src/ml/flat_backend_ML.cpp
        src/svg/flat_backend_SVG.cpp
        src/flatvectors/curves.cpp
        src/flatvectors/flatvectors.cpp
        src/flatvectors/font.cpp
        src/flatvectors/render.cpp
        src/flatvectors/utf8.cpp
        src/flatvectors/pack.cpp)

add_library(Flat SHARED ${SOURCE_FILES})
#add_executable(Flat ${SOURCE_FILES} main.cpp)

target_link_libraries(Flat opengl32 gdi32 glfw3.a -static-libstdc++)

#g++ -shared -o flat.dll -std=c++11 -Iinclude -Iinclude/nanovg -Isrc/gl -Isrc/ml -Isrc/wl -Isrc/svg -Isrc/flatvectors -I"C:/Program Files/Java/jdk-17/include" -I"C:/Program Files/Java/jdk-17/include/win32" -Llib src/glad.c src/wl/flat_backend_WL.cpp src/gl/flat_backend_GL.cpp src/ml/flat_backend_ML.cpp src/svg/flat_backend_SVG.cpp src/flatvectors/curves.cpp src/flatvectors/flatvectors.cpp src/flatvectors/font.cpp src/flatvectors/render.cpp src/flatvectors/utf8.cpp lib/libglfw3.a -lopengl32 -lgdi32 -static-libstdc++