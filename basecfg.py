# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
import yaml
import inspect
import sys

# 最后用来承载配置的内容，相当于上下文
taskChains = list()

with open("config.yaml", 'r') as stream:
    try:
        ctx = yaml.safe_load(stream)
        for profile in ctx["ActiveProfiles"]:
            module = __import__(profile + "." + profile, fromlist=True)
            cls = getattr(module, profile)
            taskChains.append(cls())
        stream.close()
    except yaml.YAMLError as exc:
        print(exc)