# -*- coding: utf8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
from configparser import ConfigParser, ExtendedInterpolation
import datetime, calendar
from datetime import timedelta

# 最后用来承载配置的内容，相当于上下文
ctx = dict()

config = ConfigParser(interpolation=ExtendedInterpolation())
config.read(r"configure.ini", encoding="utf-8")

month_from = 0
month_to = 0

if int(config["Global"]["use_cfg_month_scope"]) == 1:
    month_to = int(config["Global"]["month_from"])
    month_from = int(config["Global"]["month_to"])
else:
    # 初始化当前的月份
    month_from = datetime.datetime.now().month
    # 如果统计的是上一个月的
    base_on_previous_month = int(config["Global"]["base_on_previous_month"])

    if base_on_previous_month == 1:
        month_from = month_from - 1

    # 基于上一个月的处理，现在月份是0，实际月份是1，
    if month_from == 0:
        month_from = 12
        month_to = 11
    elif month_from == 1:
        month_to = 12
    else:
        month_to = month_from - 1

# 关于月份设置
ctx["month_scope"] = list()
ctx["month_scope_legends"] = list()
for i in range(month_to, month_from+1):
    ctx["month_scope"].append(str(i))
    ctx["month_scope_legends"].append(str(i) + '月')
ctx["month_from"] = str(month_from)
ctx["month_to"] = str(month_to)
ctx["legend_month_from"] = str(month_from) + '月'
ctx["legend_month_to"] = str(month_to) + '月'


# 关于展示参数设置
ctx['font_color'] = config['ChartSettings']['font_color']

# 设置文件资源相关的参数
ctx['data_file'] = config['Resources']['data_file']

if __name__ == 'main':
    print(ctx)

