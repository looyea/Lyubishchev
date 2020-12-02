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
    pivotTables = list()
    for month in ctx["month_scope"]:
        pivotTables.append(pd.pivot_table(
            data[month],
            values=duration,
            index=time_class,
            aggfunc=aggrfunc)
        )
    pivotTables = pd.concat(pivotTables, axis=1)
    pivotTables.columns = ctx["month_scope_legends"]
    return pivotTables


# 支持序列的月份统计处理表
def event_class_static_range(data, aggrfunc=np.sum):
    sub_idx = [time_class, event_class]
    pp = ctx['month_scope_legends']
    pivotTables = list()
    idx_set = tuple()
    for month in ctx["month_scope"]:
        pivotTable = pd.pivot_table(
            data[month],
            values=duration,
            index=time_class,
            aggfunc=aggrfunc)
        pivotTables.append(pivotTable)
        idx_set = idx_set + tuple(pivotTable.index.values)

    idx_set = np.hstack(idx_set)
    idx_set = np.unique(idx_set)

    img_df = pd.DataFrame(columns=sub_idx + pp, dtype=np.long)
    img_df.set_index(sub_idx, inplace=True)

    for idx in idx_set:
        durations = []
        for pivotTable in pivotTables:
            durations.append(pivotTable.loc[idx, duration] if idx in pivotTable.index else 0)
        img_df.loc[idx, pp] = durations
    return img_df


if __name__ == 'main':
    print(ctx)
