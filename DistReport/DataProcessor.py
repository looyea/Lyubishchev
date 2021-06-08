import numpy as np
import pandas as pd


class DataProcessor:

    _base_tag = '历时分钟1'

    def __init__(self, ctx=None):
        self.__ctx__ = ctx

        # 初始化相关的数据容器
        summaries = dict()
        for month in self.__ctx__["month_to_do"]:
            summaries[month] = dict()
            # 构建数据数组相关内容
            base_list = [0] * 2400
            summaries[month]["time_sum"] = base_list
            # 构建时间总分类的时段累计
            for str in self.__ctx__["time_idx"]:
                summaries[month][str] = base_list[:]

            for str in self.__ctx__["l1_evt_idx"]:
                summaries[month][self.__ctx__["l1_time_tag"] + "_" + str] = base_list[:]

            for str in self.__ctx__["l2_evt_idx"]:
                summaries[month][self.__ctx__["l2_time_tag"] + "_"  + str] = base_list[:]
        self.__ctx__["summaries"] = summaries

        _xstick = [0] * 2400
        for x in range(0, 2400):
            _xstick[x] = x
        self.__ctx__["xStick"] = _xstick

    def data_washing(self):
        # a stub not used by this one
        pass

    def data_transform(self):
        data = self.__ctx__["data"]
        summaries = self.__ctx__["summaries"]
        for key in self.__ctx__["month_to_do"]:
            time_table = summaries[key]
            month_data = data[key]
            for idx in month_data.index:
                # 获取当前记录行
                line = month_data.loc[idx]

                # 转换时间标签为坐标地址
                start_time = int(str(line["开始时间"]).replace(":", "")[:4])
                end_time = int(str(line["截止时间"]).replace(":", "")[:4])

                # 设置标签信息, 减少运算次数
                lable_time_class = line['时间分类']
                label_event_class = line['时间分类'] + '_' + line['事件']

                # 对应坐标地址加和
                for i in range(start_time, end_time + 1):
                    time_table["time_sum"][i] += 1
                    time_table[lable_time_class][i] += 1
                    time_table[label_event_class][i] += 1


    def data_adapting(self):
        # a stub not used by this one
        pass

    def process(self):

        # piovt清洗
        self.data_washing()

        # pivot变换
        self.data_transform()

        # 适配图形数据输出
        self.data_adapting()

        print("数据处理完毕！")
