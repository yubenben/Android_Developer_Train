#!/usr/bin/env python
#encoding=utf-8

import os
import sys
import commands
import codecs
import time
import platform
import xml.etree.ElementTree as ET
from StringIO import StringIO
import util

ndk_build_xml_str = """<?xml version="1.0" encoding="UTF-8"?>
<project>
<condition property="cmd" value=".cmd" else=""><os family="windows" /></condition>
    <target name="-pre-build" depends="-ndk-build">
    </target>

    <target name="-ndk-build">
        <exec executable="ndk-build${cmd}" failonerror="true" />
    </target>
</project>
"""

signature_properties_str = "\
key.store=%s/common/signature/android.keystore\n\
key.alias=cqzj\n\
key.store.password=cqzj_lk\n\
key.alias.password=cqzj_lk\n\
"

ostype = sys.platform

def config(workspace):

    extern_jar = []
    islib = ""
    ref_library = []

    print "\n处理workspace = %s " % workspace

    if not os.path.isdir(workspace):
        util.print_E("ERROR: 文件夹不存在")
        return

    #.classpath  project.properties local.properties
    #sed -i 's@\\@/@g' project.properties
    for f in [".classpath", "project.properties", "local.properties"]:
        if os.path.isfile(os.path.join(workspace, f)):
          sed_cmd = "sed -i 's@\\\\@/@g' %s" % (os.path.join(workspace, f))
          util.print_D(sed_cmd)
          os.system(sed_cmd)

    #获得项目名 --name
    doc_project = ET.parse(os.path.join(workspace, ".project"))
    project_name = doc_project.find("name").text
    util.print_D(project_name)

    #获得external jar   
    doc_classpath = ET.parse(os.path.join(workspace, ".classpath"))
    for node in doc_classpath.findall("classpathentry"):
        if node.get("kind", default=None) == "lib":
            extern_jar.append(node.get("path", default=None))
    util.print_D(extern_jar)

    #获得android.library
    fproject_properties = open(os.path.join(workspace, "project.properties"), "r+")
    for line in fproject_properties:
        util.print_D(line)
        if line.startswith("android.library="):
            islib = line.split('=')[1].strip()

    if ostype == "cygwin" or ostype == "win32":
        android_bat = "android.bat"
    else:
        android_bat = "android"

    #运行android.bat 脚本生成ant配置文件
    if not islib.lower() == "true":
        command = "%s update project --path %s --name %s --target android-19" \
            % (android_bat, workspace, project_name)
    else :
        command = "%s update lib-project --path %s --target android-19" \
            % (android_bat, workspace)
    if os.path.isfile(os.path.join(workspace, "build.xml")):
        util.print_W("WARNNING: 该项目已经存在build.xml文件，如需重新生成，请删除该项目中的build.xml文件")
        status,output = 0, ""
    else:
        util.print_D(command)
        (status, output) = commands.getstatusoutput(command)
        print output
    if (not status == 0) or (output.find("Error:") > -1):
         util.print_E("ERROR: ant配置文件更新失败")
    else:
        #读取android.library.reference.n 生成依赖库中的ant配置文件
        fproject_properties.seek(0)
        for line in fproject_properties:
            if line.startswith("android.library.reference."):
                ref_library.append(line.split('=')[1].strip().replace("\\", "/"))
        util.print_D(ref_library)

        #将external jar 写入project.properties文件
        if len(extern_jar) > 0:
            fproject_properties.seek(0)
            lines = fproject_properties.readlines()
            new_lines = []
            skip = False
            for line in lines:
                if line.startswith("java.compiler.classpath="):
                    new_lines.append("java.compiler.classpath=" + ";".join(extern_jar) + "\n")
                    skip = True
                else:
                    new_lines.append(line)
            if not skip:
                new_lines.append("\njava.compiler.classpath=" + ";".join(extern_jar))
                new_lines.append("\n");
            util.print_D(new_lines)
            fproject_properties.truncate(0)
            fproject_properties.seek(0)
            fproject_properties.write("".join(new_lines))
            fproject_properties.close()

        #如果有jni目录，添加ndk编译
        if os.path.isdir(os.path.join(workspace, "jni")):
            util.print_W("添加NDK编译")
            util.print_D(ndk_build_xml_str)
            fcustom_rule = open(os.path.join(workspace, "custom_rules.xml"), "w") 
            fcustom_rule.write(ndk_build_xml_str)
            fcustom_rule.close()

        #添加签名信息
        if not islib.lower() == "true":
            current = os.path.abspath(workspace)
            while True:
                if os.path.isdir(os.path.join(current, "common")):
                    break
                else:
                    current = os.path.join(current, "..")
            signature_properties_string = (signature_properties_str % current.split(os.path.basename(os.path.abspath(workspace))+"/")[1])
            fant_proper = open(os.path.join(workspace, "ant.properties"), "w")
            fant_proper.write(signature_properties_string)
            fant_proper.close()

        #生成依赖库的ant脚本
        for lib_dir in ref_library:
            config(os.path.join(workspace, lib_dir))

        #如果lib需要混淆，则把lib的proguard-project.txt include到本工程中proguard-project.txt
        fproguard = open(os.path.join(workspace, "proguard-project.txt"), "a")
        for lib_dir in ref_library:
            f = open(os.path.join(workspace, lib_dir, "project.properties"), "r")
            for line in f.readlines():
                if line.startswith("proguard.config="):
                    fproguard.write("\n-include " + os.path.join(lib_dir, "proguard-project.txt") + "\n") 
                    current = os.path.abspath(workspace)
                    while True:
                        if os.path.isdir(os.path.join(current, "common")):
                            break
                        else:
                            current = os.path.join(current, "..")
                    fproguard.write("\n-include " + os.path.join(current.split(os.path.basename(os.path.abspath(workspace))+"/")[1], \
                                "common", "LK_AntiRobot", "base-proguard-project.txt") + "\n") 
                    os.system("sed -i 's/^#proguard.config=/proguard.config=/' " + os.path.join(workspace, "project.properties"))
                    util.print_W("\n" + workspace + " 混淆打开")
            f.close()
        fproguard.close()
                    


def _help():
    util.print_W("""用法：
    ant_configure.py -h 
        显示帮助
    ant_configure.py -p path 
        path 为指定要编译的sdk平台项目路径, 如：china-alliance/91/3.2.6.1/interface_android/LK_Android_91Demo
    ant_configure.py path 
        path 为包含config.xml的目录 根据config文件引导用户，选择对应的目录进行配置
    ant_configure.py
    同ant_configure.py path 将当前目录作为path传入 当前目录含有config.xml文件""")

if __name__ == "__main__":
    
    reload(sys)
    sys.setdefaultencoding('utf-8')

    argc = len(sys.argv)
    if argc > 2:
        if(sys.argv[1] == "-p"):
            project = sys.argv[2]
            config(os.path.abspath(project))
            print "\n配置完成"
            sys.exit(0)
        else:
            util.print_E("输入的参赛不对")
            _help()
            sys.exit(1)
    else:
        if (argc > 1):
            if(sys.argv[1] == "-h"):
                _help()
                sys.exit(0)
            p_dir = sys.argv[1]
        else:
            p_dir = "."
    xmlfile = os.path.join(p_dir, "config.xml")

    if(os.path.isfile(xmlfile)):
        pathList = []
        util.go_through_xml(xmlfile, pathList)
        if ostype == "cygwin" or ostype == "win32":
            project_path = os.path.join(*pathList)
        else:
            project_path = os.path.abspath(os.path.join(*pathList))
        util.print_D(project_path)
        config(project_path)
        print "\n配置完成"
    else:
        util.print_E(xmlfile + " : can't find the file!!!")
        sys.exit(1)



#///////////////////////////////////////////////////////////
#下面的代码不再使用,仅作保留备份
#///////////////////////////////////////////////////////////
def run(project_dir):
    if not os.path.isdir(project_dir):
        util.print_E("ERROR: 指定的目录 %s 不存在" % project_dir)
        return
    inifile = os.path.join(project_dir, "project.ini")
    if os.path.isfile(inifile):
        f = open(inifile, "r")
        project = f.read()
        f.close()
        workspace = os.path.join(project_dir, project.strip())
        config(workspace)
        print "\n结束！"
    else:
        util.print_E("ERROR: 找不到配置文件信息 %s" % inifile)

def del_help():
    util.print_W("""用法：
    ant_configure.py -h 
        显示帮助
    ant_configure.py -p path 
        path 为指定要编译的sdk平台路径, 含有project.ini文件    如：....../trunk/wzzj_platform/gfan/
    ant_configure.py path 
        path 为包含各种sdk平台目录的仓库路径    如：....../trunk/wzzj_platform/
    ant_configure.py
    同ant_configure.py path 将当前目录作为path传入""")

if __name__ == "del__main__":

    reload(sys)
    sys.setdefaultencoding('utf-8')

    argc = len(sys.argv)
    if argc > 2:
        if(sys.argv[1] == "-p"):
            project = sys.argv[2]
            run(os.path.abspath(project))
        else:
            util.print_E("输入的参赛不对")
            help()
            sys.exit(1)
    else:
        if (argc > 1):
            if(sys.argv[1] == "-h"):
                help()
                sys.exit(0)
            gm_dir = sys.argv[1]
        else:
            gm_dir = "."
        project_dirs = []
        util.print_D(os.path.abspath(gm_dir))
        util.print_D(os.listdir(os.path.abspath(gm_dir)))
        for directory in os.listdir(os.path.abspath(gm_dir)):
            absdir = os.path.join(os.path.abspath(gm_dir), directory)
            if os.path.isdir(absdir) and not directory.startswith('.'):
                project_dirs.append(directory);

        util.print_D(project_dirs)
        print "请选择您要配置ant的项目名称："
        for i, project_dir in enumerate(project_dirs):
            print "\t%d. %s" % (i, project_dir)

        while True:
            try:
                index = int(raw_input("请输入编号数字："))
                break
            except ValueError:
                print "输入错误，请输入项目名前的标号数字。"
        print "configure project %d. %s" % (index, project_dirs[index])
        run(os.path.join(os.path.abspath(gm_dir), project_dirs[index]))
