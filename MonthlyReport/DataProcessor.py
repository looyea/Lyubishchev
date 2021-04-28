import numpy as np
import pandas as pd


class DataProcessor:

    _base_tag = '历时分钟1'

    def __init__(self, ctx=None):
        self.__ctx__ = ctx

    def data_washing(self):
        l1_evt_idx = self.__ctx__['l1_evt_idx']
        self.__ctx__['l1_evt_idx'] = l1_evt_idx
        l2_evt_idx = self.__ctx__['l2_evt_idx']
        self.__ctx__['l2_evt_idx'] = l2_evt_idx
        len1 = len(l1_evt_idx)
        len2 = len(l2_evt_idx)

        df1 = pd.DataFrame(data=np.zeros((len1,), dtype=int), index=l1_evt_idx, columns=[self._base_tag])
        df2 = pd.DataFrame(data=np.zeros((len2,), dtype=int), index=l2_evt_idx, columns=[self._base_tag])
        df1.index.name = '事件'
        df2.index.name = '事件'

        self.__ctx__['l1_evt_base'] = df1
        self.__ctx__['l2_evt_base'] = df2

    def __merge(self, base, data):

        base = pd.concat([base, data], axis=1, join='outer')
        base = base.drop(columns=[self._base_tag])
        base = base.fillna(0)
        return base

    def data_transform(self):
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
        del (eventpt)

        print(l1event)

        results['l1pt'] = l1pt
        results['l1_values'] = l1event
        results['l2_values'] = l2event

    def data_adapting(self):
        l1df = self.__ctx__['results']['l1_values']
        l2df = self.__ctx__['results']['l2_values']

        l1_values = list()
        l2_values = list()
        for idx in l1df.index:
            l1_values.append(l1df.loc[idx])
        l1_values.append(l1_values[0])
        for idx in l2df.index:
            l2_values.append(l2df.loc[idx])
        l2_values.append(l2_values[0])

        self.__ctx__['results']['l1_values'] = l1_values
        self.__ctx__['results']['l2_values'] = l2_values


    def process(self):

        # piovt清洗
        self.data_washing()

        # pivot变换
        self.data_transform()

        # 适配图形数据输出
        self.data_adapting()

        print("数据处理完毕！")
