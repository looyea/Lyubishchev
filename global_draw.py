# -*- coding: utf-8 -*-
"""
Created on Sun Oct  4 14:45:35 2020

@author: looyea
"""
from basecfg import ctx

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

    for a, b, c in zip(pivot_table[ctx["legend_pre_month"]], pivot_table[ctx["legend_cur_month"]], axes2.get_yticks()):
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
    for a, b, c in zip(pivot_table[ctx["legend_pre_month"]], pivot_table[ctx["legend_cur_month"]], axes3.get_yticks()):
        axes3.text(a + 250, c - 0.125, "%.0f" % a, ha="right", va="center", fontsize='small', color=ctx['font_color'])
        axes3.text(b + 250, c + 0.125, "%.0f" % b, ha="right", va="center", fontsize='small', color=ctx['font_color'])
    axes3.legend()