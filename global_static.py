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
        
    a = list()
    for idx in first_month_pivot_table.index:
       x = first_month_pivot_table.loc[idx][duration]
       y = second_month_pivot_table.loc[idx][duration]
       a.append([x,y])
    img_df=pd.DataFrame(a,index=first_month_pivot_table.index,columns=[first_month + '月', second_month + '月'])
    return img_df
    
#  f = pd.pivot_table(f, values='历时分钟', index=['时间分类','事件'], aggfunc=np.sum)

# 下一步改成支持序列month的能力
def event_class_static(data, first_month, second_month, aggrfunc=np.sum):
    sub_idx = [time_class, event_class]
    pp = [ctx['legend_month_from'], ctx['legend_month_to']]
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
    
    idx_set = np.hstack((fm.index.values, sm.index.values))
    idx_set = np.unique(idx_set)
    
    fm, sm = process_event_sub_class(
        sub_idx + [duration], 
        fm,
        sm,
        sub_idx,
        idx_set
        )

    
    img_df=pd.DataFrame(columns=sub_idx + pp, dtype=np.long)
    img_df.set_index(sub_idx, inplace=True)
    
    for idx in idx_set:
        img_df.loc[idx, pp] = [fm.loc[idx][duration], sm.loc[idx][duration]]
        
        
    return img_df


def event_sub_class_static(data, first_month, second_month, aggrfunc=np.sum):
    sub_idx = [time_class, event_class, event_sub_class]
    pp = [ctx['legend_month_from'], ctx['legend_month_to']]
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
    
    idx_set = np.hstack((fm.index.values, sm.index.values))
    idx_set = np.unique(idx_set)
    
    fm, sm = process_event_sub_class(
        sub_idx + [duration], 
        fm,
        sm,
        sub_idx,
        idx_set
        )
   
    img_df=pd.DataFrame(columns=sub_idx + pp)
    img_df.set_index(sub_idx, inplace=True)
    
    for idx in idx_set:
        img_df.loc[idx, pp] = [fm.loc[idx][duration],sm.loc[idx][duration]]
   
    return img_df

# 因为没有设置总体的分析层级，这里只能这么写，以后使用了完整多层级，会正常使用多层级索引
def process_event_sub_class(cols, fm, sm, idx_cols, idx_set, init_val=0):
    df1 = pd.DataFrame(columns=cols)
    df1.set_index(idx_cols, inplace=True)
    df2 = df1.copy(deep=True)
    
    for idx in idx_set:
        df1.loc[idx, duration] = fm.loc[idx, duration] if idx in fm.index else 0
        df2.loc[idx, duration] = sm.loc[idx, duration] if idx in sm.index else 0
        
    return df1, df2

def monthly_time_class_trend(data, month):
    pvt = pd.pivot_table(
        data[month], 
        values='历时分钟', 
        index=['日期'], 
        columns='时间分类', 
        aggfunc=np.sum)
    pvt = pvt.fillna(0)
    
    return pvt


if __name__ == 'main':
    print(ctx)