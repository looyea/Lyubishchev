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
    

def global_draw(pivot_table):
    axes = pivot_table.plot(
        kind='barh',
        width=0.3,
        mark_right=True,
        subplots=False,
        legend=False
    )

    for a, b in zip(pivot_table['历时分钟'], axes.get_yticks()):
        axes.text(a / 2, b, a, ha="right", va="center", color=ctx['font_color'])


def global_bi_month_compare(pivot_table):
    axes2 = pivot_table.plot(
        kind='barh',
        width=0.3,
        mark_right=True,
        subplots=False,
        legend=False
    )

    for a, b, c in zip(pivot_table[ctx["legend_month_from"]], pivot_table[ctx["legend_month_to"]], axes2.get_yticks()):
        print(a,b,c)
        axes2.text(a, c - 0.075, a, ha="right", va="center", color=ctx['font_color'])
        axes2.text(b, c + 0.075, b, ha="right", va="center", color=ctx['font_color'])
    axes2.legend()

def draw_event_class_static(pivot_table):
    axes3 = pivot_table.plot(
        kind='barh',
        width=0.5,
        mark_right=True,
        subplots=False,
        legend=False
    )
    for a, b, c in zip(pivot_table[ctx["legend_month_from"]], pivot_table[ctx["legend_month_to"]], axes3.get_yticks()):
        print(a,b,c)
        axes3.text(a + 250, c - 0.125, "%.0f" % a, ha="right", va="center", fontsize='small', color=ctx['font_color'])
        axes3.text(b + 250, c + 0.125, "%.0f" % b, ha="right", va="center", fontsize='small', color=ctx['font_color'])
    axes3.legend()