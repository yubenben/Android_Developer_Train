#!/usr/bin/env python
#encoding=utf-8

import os
import sys
import shutil
import xml.etree.ElementTree as ET
from collections import deque

import util

#
#jar库包括libs下的.jar包，编译生成的bin/claases.jar
#根据project.properties获取递归获取依赖库工程jar文件
#
def add_lib_ref_list(srcdir, outdir, sque, dque):
    #读取android.library.reference.n 生成依赖库中的ant配置文件
    ref_library = []
    fproject_properties = open(os.path.join(srcdir, "project.properties"), "r")
    for line in fproject_properties:
        if line.startswith("android.library.reference."):
            ref_library.append(line.split('=')[1].strip().replace("\\", "/"))
    fproject_properties.close()
    util.print_D(ref_library)

    #获得项目名 --name
    doc_project = ET.parse(os.path.join(srcdir, ".project"))
    project_name = doc_project.find("name").text
    util.print_D(project_name)

    for file in os.listdir(os.path.join(srcdir, "libs")):
        if (os.path.splitext(file)[1] == ".jar"):
            sque.append(os.path.join(srcdir, "libs", os.path.basename(file)))
            dque.append(os.path.join(outdir, "libs", os.path.basename(file)))
    
    if os.path.isdir(os.path.join(srcdir, "jni")):
        if (not os.path.isdir(os.path.join(srcdir, "libs", "armeabi"))):
            util.print_E("ERROR: " + srcdir + "native code is not build!!!")
            sys.exit(-1)
        for d in os.listdir(os.path.join(srcdir, "libs")):
            if("mips armeabi-v7a armeabi x86".find(os.path.basename(d)) > -1):
                sque.append(os.path.join(srcdir, "libs", os.path.basename(d)))
                dque.append(os.path.join(outdir, "libs", os.path.basename(d)))

    if (os.path.isfile(os.path.join(srcdir, "bin", "classes.jar"))):
        sque.append(os.path.join(srcdir, "bin", "classes.jar"))
        dque.append(os.path.join(outdir, "libs", project_name + ".jar"))
    else:
        util.print_E("ERROR: " + srcdir + " has not build !!!")
        sys.exit(-1)
    
    if (os.path.isfile(os.path.join(srcdir, "bin", "proguard", "obfuscated.jar"))):
        sque.append(os.path.join(srcdir, "bin", "proguard", "obfuscated.jar"))
        dque.append(os.path.join(outdir, "libs", project_name + "_proguard_obfuscated.jar"))

    #查询依赖库
    for lib_dir in ref_library:
        add_lib_ref_list(os.path.join(srcdir, lib_dir), outdir, sque, dque)


#添加目标工程的资源文件res,assets
def add_res_list(srcdir, outdir, sque, dque):
    sque.append(os.path.join(srcdir, "res"))
    dque.append(os.path.join(outdir, "res"))
    sque.append(os.path.join(srcdir, "assets"))
    dque.append(os.path.join(outdir, "assets"))

#如果是一个app工程，只拷贝.apk
def add_apk_list(srcdir, outdir, sque,dque):
    #获得项目名 --name
    doc_project = ET.parse(os.path.join(srcdir, ".project"))
    project_name = doc_project.find("name").text
    util.print_D(project_name)

    if (os.path.isfile(os.path.join(srcdir, "bin", project_name+"-release.apk"))):
        sque.append(os.path.join(srcdir, "bin", project_name+"-release.apk"))
        dque.append(os.path.join(outdir, project_name+"-release.apk"))
    elif (os.path.isfile(os.path.join(srcdir, "bin", project_name+"-release-unsigned.apk"))):
        sque.append(os.path.join(srcdir, "bin", project_name+"-release-unsigned.apk"))
        dque.append(os.path.join(outdir, project_name+"-release-unsigned.apk"))
    elif (os.path.isfile(os.path.join(srcdir, "bin", project_name+"-debug.apk"))):
        sque.append(os.path.join(srcdir, "bin", project_name+"-debug.apk"))
        dque.append(os.path.join(outdir, project_name+"-debug.apk"))
    else:
        util.print_E("ERROR: " + srcdir + " has not build !!!")
        sys.exit(-1)

#判断目标工程是一个app还是库
def add_out_release_list(srcdir, outdir, sque, dque):
    print os.getcwd()
    islib = ""
    fproject_properties = open(os.path.join(srcdir, "project.properties"), "r")
    for line in fproject_properties:
        if line.startswith("android.library="):
            islib = line.split('=')[1].strip()
    fproject_properties.close()

    if islib.lower() == "true":
        add_lib_ref_list(srcdir, outdir, sque, dque)
        add_res_list(srcdir, outdir, sque, dque)
    else:
        add_apk_list(srcdir, outdir, sque, dque)

#根据生成的序列，拷贝文件
def copy_result_list(sque, dque):
    while True:
        if (len(sque) == 0 or len(dque) == 0):
            break
        src = sque.popleft()
        dst = dque.popleft()
        util.print_V(src + " => " + dst + "\n")
        copy_(src, dst)
        util.print_D(src + " => " + dst)


def copy_(src, dst):
    if(os.path.isdir(src)):
        util.copytree(src, dst)
    elif(os.path.isfile(src)):
        util.print_D(src + " => " + dst)
        if (not os.path.isdir(os.path.dirname(dst))):
            os.makedirs(os.path.dirname(dst))
        shutil.copyfile(src, dst)
    else:
        util.print_E("ERROR: " + src + " is not either a file or dir")
        sys.exit(-1)


#删除拷贝目录是拷贝的.svn目录
def del_svn(dirname):
    for i in os.walk(dirname):
        if (os.path.basename(i[0]) == ".svn"):
            util.print_D("rm " + i[0])
            shutil.rmtree(i[0])  

#对外调用，拷贝srcname的生成文件到dstname
def copy_out(srcname, dstname):
    util.print_D("srcname : " + srcname)
    util.print_D("dstname : " + dstname)

    srcqueue = deque([])
    dstqueue = deque([])

    if not os.path.isdir(dstname):  
        os.makedirs(dstname)

    add_out_release_list(srcname, dstname, srcqueue, dstqueue)
    copy_result_list(srcqueue, dstqueue)
    del_svn(dstname)
    print "拷贝完成"


if __name__ == "__main__":

    srcqueue = deque([])
    dstqueue = deque([])
    srcdir = os.getcwd()
    dstdir = os.path.join(os.getcwd(), "..", "release")

    if os.path.isdir(dstdir):  
        print dstdir
        util.print_W("release文件夹已存在，清除文件夹内容重新拷贝?")
        ans = raw_input("y删除原来文件夹，n直接覆盖原文件夹(y/n):")
        if ans.lower() == "y":
            shutil.rmtree(dstdir)
            os.makedirs(dstdir)
    elif os.path.isfile(dstdir):
        print dstdir
        ans = raw_input("release是一个文件，删除该文件?(y/n)")
        if ans.lower() == "y":
            os.remove(dstdir)
            os.makedirs(dstdir)
        else:
            util.print_E("拷贝失败！请确认release文件内容，或者备份之后再执行该脚本。")
    else:
        os.makedirs(dstdir)

    add_lib_ref_list(srcdir, dstdir, srcqueue, dstqueue)
    add_res_list(srcdir, dstdir, srcqueue, dstqueue)
    copy_result_list(srcqueue, dstqueue)
    del_svn(dstdir)
    print "拷贝完成"

