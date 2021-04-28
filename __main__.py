# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
from basecfg import taskChains

for taskChain in taskChains:

    taskChain.data_process()
    taskChain.chart_process()
    taskChain.output_process()
    taskChain.release()
