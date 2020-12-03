# -*- coding: utf-8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
import pandas as pd
import numpy as np
from basecfg import ctx

duration = '历时分钟'
time_class = '时间分类'
event_class = '事件'
event_sub_class = '子分类'


def current_month(data, month, aggrfunc=np.sum):
    month_pivot_table = pd.pivot_table(
        data[month],
        values=duration,
        index=time_class,
        aggfunc=aggrfunc)

    return month_pivot_table


# 时间层级，多月份支持，时间层级，目前就I\II\III三类，不需要扩展
def time_class_static_range(data, aggrfunc=np.sum):
    pivotTables = pd.DataFrame()
    for month in ctx["month_scope"]:
        pivotTable = pd.pivot_table(
            data[month],
            values=duration,
            index=time_class,
            aggfunc=aggrfunc)
        pivotTables.concat([pivotTables, pivotTable], axis=1)
    pivotTables = pivotTables.fillna(0)
    pivotTables.columns = ctx["month_scope_legends"]
    return pivotTables


# 支持序列的月份统计处理表
def event_class_static_range(data, aggrfunc=np.sum):
    sub_idx = [time_class, event_class]
    resultTable = pd.DataFrame()
    for month in ctx["month_scope"]:
        pivotTable = pd.pivot_table(
            data[month],
            values=duration,
            index=sub_idx,
            aggfunc=aggrfunc)
        resultTable = pd.concat([resultTable, pivotTable], axis=1)
    resultTable = resultTable.fillna(0)
    resultTable.columns = ctx["month_scope_legends"]
    return resultTable


if __name__ == 'main':
    print(ctx)
