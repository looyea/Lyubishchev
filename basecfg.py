# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
from configparser import ConfigParser, ExtendedInterpolation
import sys #  模块，sys指向这个模块对象
import inspect

# 最后用来承载配置的内容，相当于上下文
ctx = dict()

config = ConfigParser(interpolation=ExtendedInterpolation())
config.read(r"configure.ini", encoding="utf-8")

profiles = config["ActiveProfiles"]["profiles"].split(",")
taskChains = list()

for profile in profiles:
    module = __import__(profile + "." + profile, fromlist=True)
    # module.sayHi()
    taskChains.append(module)

print("任务链加载完毕...")