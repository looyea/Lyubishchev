import numpy as np
import pandas as pd


class DataProcessor:

    _base_tag = '历时分钟1'

    def __init__(self, ctx=None):
        self.__ctx__ = ctx

    def data_washing(self):
        # 这里用来创建一个基础的DF，用来方便合并DF并且进行数据清洗
        l1_evt_idx = self.__ctx__['l1_evt_idx']
        l2_evt_idx = self.__ctx__['l2_evt_idx']
        len1 = len(l1_evt_idx)
        len2 = len(l2_evt_idx)

        df1 = pd.DataFrame(data=np.zeros((len1,), dtype=int), index=l1_evt_idx, columns=[self._base_tag])
        df2 = pd.DataFrame(data=np.zeros((len2,), dtype=int), index=l2_evt_idx, columns=[self._base_tag])
        df1.index.name = '事件'
        df2.index.name = '事件'

        self.__ctx__['l1_evt_base'] = df1
        self.__ctx__['l2_evt_base'] = df2

        time_idx = self.__ctx__['time_idx']
        len3 = len(time_idx)
        df3 = pd.DataFrame(data=np.zeros((len3,), dtype=int), index=time_idx, columns=[self._base_tag])
        self.__ctx__['time_base'] = df3

    def data_transform(self):
        data = self.__ctx__['data']

        results = dict()
        self.__ctx__['results'] = results

        # 循环当前已经设定的月份，查找相关的数据
        for month in self.__ctx__['month_to_do']:
            l1pt = pd.pivot_table(
                data[month],
                values=self.__ctx__['sum_tag'],
                index=self.__ctx__['l1_agg_tag'],
                aggfunc=np.sum)
            l1pt.columns = [month]
            self.__ctx__['time_base'] = pd.concat([self.__ctx__['time_base'], l1pt], axis=1, join='outer')

            eventpt = pd.pivot_table(
                data[month],
                values=self.__ctx__['sum_tag'],
                index=self.__ctx__['l2_agg_tags'],
                aggfunc=np.sum
            )
            l1event = eventpt.loc[self.__ctx__['l1_time_tag']]
            l1event.columns = [month]
            l2event = eventpt.loc[self.__ctx__['l2_time_tag']]
            l2event.columns = [month]

            self.__ctx__['l1_evt_base'] = pd.concat([self.__ctx__['l1_evt_base'], l1event], axis=1, join='outer')
            self.__ctx__['l2_evt_base'] = pd.concat([self.__ctx__['l2_evt_base'], l2event], axis=1, join='outer')

            del (eventpt)

        self.__ctx__['time_base'] = self.__ctx__['time_base'].fillna(0).drop(columns=[self._base_tag])
        self.__ctx__['l1_evt_base'] = self.__ctx__['l1_evt_base'].fillna(0).drop(columns=[self._base_tag])
        self.__ctx__['l2_evt_base'] = self.__ctx__['l2_evt_base'].fillna(0).drop(columns=[self._base_tag])

        results['time_base'] = self.__ctx__['time_base']
        results['l1_evt_base'] = self.__ctx__['l1_evt_base']
        results['l2_evt_base'] = self.__ctx__['l2_evt_base']

        del(self.__ctx__['time_base'])
        del(self.__ctx__['l1_evt_base'])
        del(self.__ctx__['l2_evt_base'])


    def data_adapting(self):
        l1df = self.__ctx__['results']['l1_evt_base']
        l2df = self.__ctx__['results']['l2_evt_base']

        l1_vals = dict()
        l2_vals = dict()

        for month in self.__ctx__['month_to_do']:

            l1_values = list()
            l2_values = list()

            for idx in l1df.index:
                l1_values.append(l1df.loc[idx][month])
            l1_values.append(l1_values[0])
            for idx in l2df.index:
                l2_values.append(l2df.loc[idx][month])
            l2_values.append(l2_values[0])

            l1_vals[month] = l1_values
            l2_vals[month] = l2_values

        self.__ctx__['results']['l1_values'] = l1_vals
        self.__ctx__['results']['l2_values'] = l2_vals

    def process(self):

        # piovt清洗
        self.data_washing()

        # pivot变换
        self.data_transform()

        # 适配图形数据输出
        self.data_adapting()

        print("数据处理完毕！")
