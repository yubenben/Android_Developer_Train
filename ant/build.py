#!/usr/bin/env python
#encoding=utf-8
import os
import sys
import commands
import codecs
import time
import util
import getopt

import copy_result

def compile_code(workspace, debug=False):
    if os.path.isdir(workspace):
        pre_dir = os.getcwd()
        os.chdir(workspace)
        print os.getcwd();
        print "编译准备。"
        if not os.path.isdir("../log"):
             os.mkdir("../log")
        logFile = open(os.path.join("../log", str(time.time())+".log"), "w")
        print "删除上次的编译结果。"
        (status, output) = commands.getstatusoutput("ant clean")
        logFile.writelines(output)
        logFile.writelines("\n\n========== ant clean finish! ==========\n\n");
        if status == 0:
             print "删除成功。开始编译。Log信息查看文件：%s" % os.path.abspath(logFile.name)
        else:
             print output
             util.print_E("删除失败，退出编译。Log信息查看文件：%s" % os.path.abspath(logFile.name))
             logFile.close()
             sys.exit(-1)
        if debug:
            util.print_V("ant debug")
            (status, output) = commands.getstatusoutput("ant debug")
        else:
            util.print_V("ant release")
            (status, output) = commands.getstatusoutput("ant release")
        logFile.writelines(output)
        logFile.writelines("\n\n========== ant release finish! ==========\n");
        if status == 0:
             print "编译成功。Log信息查看文件：%s" % os.path.abspath(logFile.name)
        else:
             print output
             util.print_E("编译失败。Log信息查看文件：%s" % os.path.abspath(logFile.name))
             sys.exit(-1)
        logFile.close()
        os.chdir(pre_dir)
    else:
        util.print_E("error: 配置信息错误，目录 %s 不存在" % workspace)
        sys.exit(-1)

def _help():
    util.print_W("""用法：build.py [选项]... [PATH]
[PATH] 为包含config.xml的目录 根据config文件引导用户，选择对应的目录进行编译。当不指定PATH时，这代表使用当前文件夹

选项参数：
    -h, --help
        显示帮助
    -p, --path=project_path 
        project_path 为指定要编译的sdk平台项目路径, 如：china-alliance/91/3.2.6.1/interface_android/LK_Android_91Demo
    -d, --debug
        指定编译debug版本，默认编译release版本""")

if __name__ == "__main__":
    
    reload(sys)
    sys.setdefaultencoding('utf-8')

    build_debug = False
    project_dir = ""

    try:
        opts, args = getopt.getopt(sys.argv[1:], "hp:d", ["help", "path=", "debug"])
    except getopt.GetoptError:
        util.print_E("参数错误\n")
        _help()
        sys.exit(2)

    for opt, arg in opts:
        if opt in ("-h", "--help"):
            _help()
            sys.exit(0)
        elif opt in ("-d", "--debug"):
            build_debug = True
        elif opt in ("-p", "--path"):
            project_dir = arg

    if len(args) > 1:
        util.print_E("参数错误\n")
        _help()
        sys.exit(2)
    elif len(args) > 0:
        p_dir = args[0]
    else:
        p_dir = ""

    util.print_D("p_dir = %s" % p_dir)
    util.print_D("build_debug = %s" % build_debug)
    util.print_D("project_dir = %s" % project_dir)

    if(len(project_dir) > 0):
        project_path = os.path.abspath(project_dir)
        release_path = os.path.abspath(os.path.join("release", project_dir))
        util.print_D(project_path)
        compile_code(project_path, build_debug)
        copy_result.copy_out(project_path, release_path)
    else:
        xmlfile = os.path.join(p_dir, "config.xml")
        if(os.path.isfile(xmlfile)):
            pathList = []
            util.go_through_xml(xmlfile, pathList)
            project_path = os.path.abspath(os.path.join(*pathList))
            release_path = os.path.abspath(os.path.join("release", *pathList))
            util.print_D(project_path)
            compile_code(project_path, build_debug)
            copy_result.copy_out(project_path, release_path)
        else:
            util.print_E(xmlfile + " : can't find the file!!!")
            sys.exit(1)
    


#///////////////////////////////////////////////////////////
#下面的代码不再使用,仅作保留备份
#///////////////////////////////////////////////////////////
def run(project_dir):
    if not os.path.isdir(project_dir):
        util.print_E("error: 指定的目录 %s 不存在" % project_dir)
        return
    inifile = os.path.join(project_dir, "project.ini")
    if os.path.isfile(inifile):
        f = open(inifile, "r")
        project = f.read()
        f.close()
        workspace = os.path.join(project_dir, project.strip())
        compile_code(workspace)
    else:
        util.print_E("error: 找不到配置文件信息 %s" % inifile)


def help():
    util.print_W("""用法：
    build.py -h 
        显示帮助
    build.py -p path 
        path 为指定要编译的sdk平台路径, 含有project.ini文件    如：....../trunk/wzzj_platform/gfan/
    build.py path 
        path 为包含各种sdk平台目录的仓库路径    如：....../trunk/wzzj_platform/
    build.py
    同build.py path 将当前目录作为path传入""")


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
        print "请选择您要编译的项目名称："
        for i, project_dir in enumerate(project_dirs):
            print "\t%d. %s" % (i, project_dir)

        while True:
            try:
                index = int(raw_input("请输入编号数字："))
                break
            except ValueError:
                print "输入错误，请输入项目名前的标号数字。"
        print "build project %d. %s" % (index, project_dirs[index])
        run(os.path.join(os.path.abspath(gm_dir), project_dirs[index]))
