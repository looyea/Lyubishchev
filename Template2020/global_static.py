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


def current_month_barh(data, month, aggrfunc=np.sum):
    month_pivot_table = pd.pivot_table(
            data[month], 
            values=duration, 
            index=time_class, 
            aggfunc=aggrfunc) 
    
    return month_pivot_table


# 下一步改成支持序列month的能力
def time_class_static(data, first_month, second_month, aggrfunc=np.sum):
    first_month_pivot_table = pd.pivot_table(
            data[first_month], 
            values=duration, 
            index=time_class, 
            aggfunc=aggrfunc) 
    second_month_pivot_table = pd.pivot_table(
            data[second_month], 
            values=duration, 
            index=time_class, 
            aggfunc=aggrfunc) 
    img_df = pd.concat([first_month_pivot_table, second_month_pivot_table], axis= 1)
    img_df.columns = [first_month + '月', second_month + '月']
    img_df = img_df.fillna(0)
    return img_df


# 下一步改成支持序列month的能力
def event_class_static(data, first_month, second_month, aggrfunc=np.sum):
    sub_idx = [time_class, event_class]
    fm = pd.pivot_table(
        data[first_month], 
        values=duration, 
        index=sub_idx, 
        aggfunc=aggrfunc)
    
    sm = pd.pivot_table(
        data[second_month], 
        values=duration, 
        index=sub_idx, 
        aggfunc=aggrfunc)
    img_df = pd.concat([fm, sm], axis=1)
    img_df.columns = [first_month + '月', second_month + '月']
    img_df = img_df.fillna(0)
    return img_df


if __name__ == 'main':
    print(ctx)