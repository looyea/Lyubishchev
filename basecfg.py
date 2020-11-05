# -*- coding: utf8 -*-
from configparser import ConfigParser, ExtendedInterpolation
import datetime, calendar
from datetime import timedelta

# 最后用来承载配置的内容，相当于上下文
ctx = dict()

config = ConfigParser(interpolation=ExtendedInterpolation())
config.read(r"configure.ini", encoding="utf-8")


# 初始化当前的月份
cur_month = datetime.datetime.now().month
pre_month = 0
# 如果统计的是上一个月的
if config["Global"]["base_on_previous_month"]:
    cur_month = cur_month - 1

# 基于上一个月的处理，现在月份是0，实际月份是1，
if cur_month == 0:
    cur_month = 12
    pre_month = 11
elif cur_month == 1:
    pre_month = 12
else:
    pre_month = cur_month - 1


ctx["cur_month"] = cur_month
ctx["pre_month"] = pre_month
ctx["legend_cur_month"] = str(cur_month) + '月'
ctx["legend_pre_month"] = str(pre_month) + '月'

# print(ctx)

