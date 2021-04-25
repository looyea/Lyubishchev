import numpy as np
import pandas as pd


class DataProcessor:

    def __init__(self, ctx=None):
        self.__ctx__ = ctx

    def __dataWashing(self):
        l1_evt_idx = self.__ctx__['l1_evt_idx'].split(",")
        l2_evt_idx = self.__ctx__['l2_evt_idx'].split(",")
        len1 = len(l1_evt_idx)
        len2 = len(l2_evt_idx)

        df1 = pd.DataFrame(data=np.zeros((len1,), dtype=int), index=l1_evt_idx, columns=['历时分钟1'])
        df2 = pd.DataFrame(data=np.zeros((len2,), dtype=int), index=l2_evt_idx, columns=['历时分钟1'])
        df1.index.name = '事件'
        df2.index.name = '事件'

        self.__ctx__['l1_evt_base'] = df1
        self.__ctx__['l2_evt_base'] = df2

    def __merge(self, base, data):

        base = pd.concat([base, data], axis=1, join='outer')
        base = base.drop(columns=['历时分钟1'])
        base = base.fillna(0)
        return base


    def __dataTransform(self):
        results = dict()
        self.__ctx__['results'] = results
        data = self.__ctx__['data']

        l1pt = pd.pivot_table(
            data[self.__ctx__['cur_month']],
            values='历时分钟',
            index='时间分类',
            aggfunc=np.sum)

        eventpt = pd.pivot_table(
            data[self.__ctx__['cur_month']],
            values='历时分钟',
            index=['时间分类', '事件'],
            aggfunc=np.sum
        )
        l1event = eventpt.loc['I类时间']
        l2event = eventpt.loc['II类时间']
        l1event = self.__merge(self.__ctx__['l1_evt_base'], l1event)
        l2event = self.__merge(self.__ctx__['l2_evt_base'], l2event)
        print(l1event, l2event)
        del (eventpt)

        results['l1pt'] = l1pt
        results['l1event'] = l1event
        results['l2event'] = l2event


    def __dataAdapting(self):
        pass

    def process(self):

        # piovt清洗
        self.__dataWashing()

        # pivot变换
        self.__dataTransform()

        # 适配数据输出
        self.__dataAdapting()

        print("数据处理完毕！")
