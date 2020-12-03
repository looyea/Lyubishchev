# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""

from basecfg import ctx
import pandas as pd
import global_static as gs
import global_static_range as gsr
import global_draw as gd
import global_draw_range as gdr


# read data from data_file
data = pd.read_excel(
    ctx['data_file'],
    sheet_name=None,
    index_col=None
)

# set up configuration for drawing
gdr.plt_set()

# 画当月份的I\II\III类时间
img_df = gsr.current_month(data, ctx["month_from"])
gdr.draw_current_month(img_df)

# 画从开始到结束月份I\II\III类时间比对
# img_df2 = gs.time_class_static_range(data, ctx["pre_month"], ctx["cur_month"])
# gd.global_bi_month_compare_range(img_df2)
img_df2 = gsr.time_class_static_range(data)
gdr.draw_time_class_static_range(img_df2)

# 画上个月，本月事件类时间对比
# img_df3 = gs.event_class_static(data, ctx["month_from"], ctx["month_to"])
# gd.draw_event_class_static(img_df3)
img_df3 = gsr.event_class_static_range(data)
gdr.draw_event_class_static_range(img_df3)