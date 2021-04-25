import numpy as np
import pandas as pd


class DataProcessor:

    def __init__(self, ctx=None):
        self.__ctx__ = ctx

    def __dataWashing(self, data):
        return data

    def __dataTransform(self, data):

        results = dict()

        l1pt = pd.pivot_table(
            data[self.__ctx__['cur_month']],
            values='历时分钟',
            index='时间分类',
            aggfunc=np.sum)
        print(l1pt)

        eventpt = pd.pivot_table(
            data[self.__ctx__['cur_month']],
            values='历时分钟',
            index=['时间分类', '事件'],
            aggfunc=np.sum
        )
        print(eventpt)
        l1event = eventpt.loc['I类时间']
        l2event = eventpt.loc['II类时间']
        print(l1event)
        print(l2event)

        results['l1pt'] = l1pt
        results['l1event'] = l1event
        results['l2event'] = l2event
        return results

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

        self.__ctx__['results'] = results
