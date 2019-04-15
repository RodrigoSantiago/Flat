cmake_minimum_required(VERSION 3.6)
project(Flat)

find_package(Java REQUIRED)
find_package(JNI REQUIRED)

set(CMAKE_CXX_STANDARD 11)

include_directories(include
        include/nanovg
        src/gl
        src/ml
        src/wl
        src/svg
        src/flatvectors
        "C:/Program Files/Java/jdk1.8.0_201/include" "C:/Program Files/Java/jdk1.8.0_201/include/win32")

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
        src/flatvectors/triangulate.cpp
        src/flatvectors/utf8.cpp)

add_library(Flat SHARED ${SOURCE_FILES})

target_link_libraries(Flat opengl32 gdi32 glfw3.a)