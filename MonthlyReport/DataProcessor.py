import numpy as np
import pandas as pd


class DataProcessor():

    def __init__(self, ctx=None):
        self.__ctx__ = ctx

    def __dataWashing(self, data):
        return data

    def __dataTransform(self, data):
        month_pivot_table = pd.pivot_table(
            data['2'],
            values='历时分钟',
            index='时间分类',
            aggfunc=np.sum)
        print(month_pivot_table)
        return month_pivot_table

    def __dataAdapting(self, data):
        return data

    def process(self):
        data = self.__ctx__['data']
        # piovt清洗
        results = self.__dataWashing(data)

        # pivot变换
        results = self.__dataTransform(results)

        # 适配数据输出
        results = self.__dataAdapting(results)

        print("数据处理完毕！")

        return results
