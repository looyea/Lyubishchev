# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""

from basecfg import ctx
import pandas as pd
import global_static as gs
import global_draw as gd

# read data from data_file
data = pd.read_excel(
    ctx['data_file'],
    sheet_name=None,
    index_col=None
)

# set up configuration for drawing
gs.plt_set()

# 画当前整体的图形
img_df = gs.current_month_barh(data, ctx["cur_month"])
gd.global_draw(img_df)

# 画上个月，本月I类时间对比
img_df2 = gs.time_clss_static(data, ctx["pre_month"], ctx["cur_month"])
gd.global_bi_month_compare(img_df2)

# 画上个月，本月II类时间对比
img_df3 = gs.event_class_static(data, ctx["pre_month"], ctx["cur_month"])
gd.draw_event_class_static(img_df3)

# 画两个月分所有的3级分类时间对比
event_sub_df = gs.event_sub_class_static(data, ctx["pre_month"], ctx["cur_month"])
event_sub_df.plot(kind='barh')

