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
            base_list = [0] * 2401
            summaries[month]["time_sum"] = base_list
            # 构建时间总分类的时段累计
            for str in self.__ctx__["time_idx"]:
                summaries[month][str] = base_list[:]

            for str in self.__ctx__["l1_evt_idx"]:
                summaries[month][self.__ctx__["l1_time_tag"] + "_" + str] = base_list[:]

            for str in self.__ctx__["l2_evt_idx"]:
                summaries[month][self.__ctx__["l2_time_tag"] + "_"  + str] = base_list[:]
        self.__ctx__["summaries"] = summaries

        # 时间坐标轴标签，因为是24小时，所以每个小时之间会有个40分钟间隔
        # 设置成2401，是因为要防止2359的时候对应2400，导致数组超出索引
        _xstick = [x for x in range(0, 2401)]
        # 做个数组索引标记位，如果当前对应数组上属于24小时内分钟客户，则标记为True
        # 相当于从0-2400，只有落在实际时间场的内容，才是True，方便后面，在打标计数的时候有用
        _xstick_idx = [False] * 2401
        for i in range(0, 24):
            for x in range(0, 60):
                _xstick_idx[i * 100 + x] = True
        self.__ctx__["xStick"] = _xstick
        self.__ctx__["xStick_idx"] = _xstick_idx

    def data_washing(self):
        # a stub not used by this one
        pass

    def data_transform(self):
        data = self.__ctx__["data"]
        summaries = self.__ctx__["summaries"]
        xstick_idx = self.__ctx__["xStick_idx"]
        for key in self.__ctx__["month_to_do"]:
            time_table = summaries[key]
            month_data = data[key]

            # 每次取一行数据
            for idx in month_data.index:
                # 获取当前记录行
                line = month_data.loc[idx]

                # 转换时间标签为坐标地址
                start_time = int(str(line["开始时间"]).replace(":", "")[:4])
                end_time = int(str(line["截止时间"]).replace(":", "")[:4])
                lable_time_class = line['时间分类']
                label_event_class = line['时间分类'] + '_' + line['事件']

                # 对应坐标地址加和, 需要兑换坐标
                for i in range(start_time, end_time+1):
                    if xstick_idx[i]:
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
