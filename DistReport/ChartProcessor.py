import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import math


class ChartProcessor:

    def __init__(self, ctx=None):
        self.__ctx__ = ctx
        plt.rcParams['font.sans-serif'] = ['Microsoft YaHei']  # 用来正常显示中文标签
        plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

        sns.set()
        sns.set_style("whitegrid")
        sns.set_palette(sns.color_palette("Accent"))
        sns.set_style({"font.sans-serif": ['Microsoft YaHei', 'SimHei']})  # 显示中文

    def process(self):
        self.draw_line_chart()
        print("图形处理完毕！")



    def draw_line_chart(self):
        for month in self.__ctx__["summaries"].keys():
            month_data = self.__ctx__["summaries"][month]
            for key in month_data.keys():
                plt.plot(self.__ctx__["xStick"], month_data[key])

        plt.show()