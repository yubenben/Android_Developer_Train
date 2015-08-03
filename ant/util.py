#!/usr/bin/env python
#encoding=utf-8

import os
import sys
import shutil
from xml.dom import minidom

DEBUG = False
#DEBUG = True

#red
def print_E(message):
    print "\033[1;31m%s\033[0m" % (message)

#yellow
def print_W(message):
    print "\033[1;33m%s\033[0m" % (message)

#blue
def print_D(message):
    if DEBUG:
        print "\033[1;34m%s\033[0m" % (message) 

#gree
def print_V(message):
    print "\033[1;32m%s\033[0m" % (message)

def copytree(src, dst, symlinks=False):  
    names = os.listdir(src)  
    if not os.path.isdir(dst):  
        os.makedirs(dst)  
              
    errors = []  
    for name in names:  
        srcname = os.path.join(src, name)  
        dstname = os.path.join(dst, name)  
        try:  
            if symlinks and os.path.islink(srcname):  
                linkto = os.readlink(srcname)  
                os.symlink(linkto, dstname)  
            elif os.path.isdir(srcname):  
                copytree(srcname, dstname, symlinks)  
            else:  
                if os.path.isdir(dstname):  
                    os.rmdir(dstname)  
                elif os.path.isfile(dstname):  
                    os.remove(dstname)  
                shutil.copy2(srcname, dstname)
                print_D(srcname + " => " + dstname)
            # XXX What about devices, sockets etc.?  
        except (IOError, os.error) as why:  
            errors.append((srcname, dstname, str(why)))  
        # catch the Error from the recursive copytree so that we can  
        # continue with other files  
        except OSError as err:  
            errors.extend(err.args[0])  
    try:  
        shutil.copystat(src, dst)  
    # except WindowsError:  
        # can't copy file access times on Windows  
        pass  
    except OSError as why:  
        errors.extend((src, dst, str(why)))  
    if errors:  
        raise Error(errors)


def go_through_xml(xml, plist):
    xmldoc = minidom.parse(xml)
    firstNode = xmldoc.documentElement
    pNode = firstNode
    while True:
        print_D(pNode)
        plist.append(pNode.getAttribute("dir_name"))
        if len(pNode.childNodes) == 0:
            break

        i = 0
        print "\n"
        for node in (pNode.childNodes):
            print_D(node)
            if (node.nodeType == node.ELEMENT_NODE):
                print "%d: %s" % (i, node.getAttribute("alias"))
                i += 1
        if (i > 1):
            num = int(raw_input("\n请输入数字编码："))
        elif i == 1:
            print_V("仅一个选项，自动选为默认")
            num = 0
        else:
            print_E(pNode.toxml())
            print_E("ERROR: " +  "内容为空, 请检查配置")
            sys.exit(0)
        print "\n"
        pNode = pNode.childNodes[2 * num + 1]

