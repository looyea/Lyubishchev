# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""

from basecfg import taskChains
import pandas as pd
import global_static as gs
import global_static_range as gsr
import global_draw as gd
import global_draw_range as gdr

for taskChain in taskChains:
    taskChain.init()
    taskChain.dataProcess()
    taskChain.chartProcess()
    taskChain.outputProcess()
    taskChain.release()
