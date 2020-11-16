# -*- coding: utf8 -*-
from basecfg import ctx
import string
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from pylab import mpl
import seaborn as sns
import global_static as gs

# 1. 程序从根目录加载配置
# 1. Main program loads configuration from root folder


# 读取完毕就是key，value形式，key是work_sheet名字，值是data frame
data = pd.read_excel(
    'E:/Codes/Lyubishchev/2020时间日志.xlsx',
    sheet_name=None,
    index_col=None
)

gs.plt_set()
img_df = gs.current_month_barh(data, cur_month)

axes = img_df.plot(
    kind='barh',
    width=0.3,
    mark_right=True,
    subplots=False,
    legend=False
)

for a, b in zip(img_df['历时分钟'], axes.get_yticks()):
    axes.text(a / 2, b, a, ha="right", va="center", color=font_color)

img_df2 = gs.time_clss_static(data, pre_month, cur_month)
axes2 = img_df2.plot(
    kind='barh',
    width=0.3,
    mark_right=True,
    subplots=False,
    legend=False
)

for a, b, c in zip(img_df2['9月'], img_df2['10月'], axes.get_yticks()):
    axes2.text(a, c - 0.075, a, ha="right", va="center", color=font_color)
    axes2.text(b, c + 0.075, b, ha="right", va="center", color=font_color)
axes2.legend()

img_df3 = gs.event_class_static(data, pre_month, cur_month)
axes3 = img_df3.plot(
    kind='barh',
    width=0.5,
    mark_right=True,
    subplots=False,
    legend=False
)
for a, b, c in zip(img_df3['9月'], img_df3['10月'], axes3.get_yticks()):
    axes3.text(a + 250, c - 0.125, "%.0f" % a, ha="right", va="center", fontsize='small', color=font_color)
    axes3.text(b + 250, c + 0.125, "%.0f" % b, ha="right", va="center", fontsize='small', color=font_color)
axes3.legend()

event_sub_df = gs.event_sub_class_static(data, pre_month, cur_month)
event_sub_df.plot(kind='barh')

