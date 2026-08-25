cmake_minimum_required(VERSION 3.22.1)
project(elpterm)

add_library(elpterm SHARED elpterm.cpp)
find_library(log-lib log)
target_link_libraries(elpterm ${log-lib})
target_compile_options(elpterm PRIVATE -Wall -Wextra -O2)
