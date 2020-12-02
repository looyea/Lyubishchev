# -*- coding: utf-8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
from basecfg import ctx
import matplotlib.pyplot as plt
import matplotlib as mpl

import seaborn as sns

def plt_set():
    plt.rcParams['font.sans-serif']=['SimHei'] #用来正常显示中文标签
    plt.rcParams['axes.unicode_minus']=False #用来正常显示负号
    # mpl.rcParams['font.sans-serif'] = ['Microsoft YaHei']    # 指定默认字体：解决plot不能显示中文问题
    # mpl.rcParams['axes.unicode_minus'] = False           # 解决保存图像是负号'-'显示为方块的问题
    
    sns.set()
    sns.set_style("whitegrid")
    sns.set_palette(sns.color_palette("Accent"))
    sns.set_style({"font.sans-serif":['Microsoft YaHei','SimHei']})#显示中文

    # 设置图形色盘
    # pal = sns.color_palette("Greens_d",len(grouped_values))
    # sns.set_palette(pal)
    

def draw_current_month(pivot_table):
    axes = pivot_table.plot(
        kind='barh',
        width=0.3,
        mark_right=True,
        subplots=False,
        legend=False
    )

    for a, b in zip(pivot_table['历时分钟'], axes.get_yticks()):
        axes.text(a / 2, b, a, ha="right", va="center", color=ctx['font_color'])


def draw_time_class_static_range(pivot_table):
    axes2 = pivot_table.plot(
        kind='barh',
        width=0.3,
        mark_right=True,
        subplots=False,
        legend=False
    )
    print(pivot_table)
    for i in zip(pivot_table[ctx["month_scope_legends"]], axes2.get_yticks()):
        print(i)
    # for a, b, c in zip(pivot_table[ctx["month_scope_legends"]], axes2.get_yticks()):
    #     axes2.text(a, c - 0.075, a, ha="right", va="center", color=ctx['font_color'])
    #     axes2.text(b, c + 0.075, b, ha="right", va="center", color=ctx['font_color'])
    axes2.legend()

def draw_event_class_static_range(pivot_table):
    axes3 = pivot_table.plot(
        kind='barh',
        width=0.5,
        mark_right=True,
        subplots=False,
        legend=False
    )
    # 组成月份1分类耗时，月份2分类耗时。。。月份n分类耗时
    list_to_zip = list()
    for i in ctx["month_scope_legends"]:
        list_to_zip.append(pivot_table[i])

    # 组成月份1分类耗时，月份2分类耗时。。。月份n分类耗时，坐标轴
    print(axes3.get_yticks())
    print(list_to_zip)
    #for durations, y_tick in zip(list_to_zip, axes3.get_yticks()):
        # print(durations, y_tick)
        # for duration in durations:
        #     axes3.text(duration + 250, ytick - 0.125, "%.0f" % duration, ha="right", va="center", fontsize='small', color=ctx['font_color'])
    axes3.legend()