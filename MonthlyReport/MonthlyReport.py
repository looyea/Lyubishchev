from configparser import ConfigParser, ExtendedInterpolation
import pandas as pd


class MonthlyReport():

    def __init__(self, data=None):
        self.__moduleName__ = "MonthlyReport"
        self.__ctx__ = dict()
        self.__data__ = data

        print(self.__moduleName__ + "初始化……")
        # 配置加载
        ctx = ConfigParser(interpolation=ExtendedInterpolation())
        ctx.read(self.__moduleName__ + "/" + self.__moduleName__ + ".ini", encoding="utf-8")
        for key in ctx[self.__moduleName__].keys():
            self.__ctx__[key] = ctx[self.__moduleName__][key]

        # 数据加载
        if data is None:
            data = pd.read_excel(
                self.__ctx__['data_source_path'],
                sheet_name=None,
                index_col=None
            )
        self.__ctx__['data'] = data

        # 处理链加载
        task_processors_names = self.__ctx__["task_chain"].split(",")
        self.__ctx__["process_chain"] = list()
        for name in task_processors_names:
            processor = __import__(self.__moduleName__ + "." + name, fromlist=True)
            cls = getattr(processor, name)
            self.__ctx__["process_chain"].append(cls(self.__ctx__))

        self.__ctx__["data_processor"] = self.__ctx__["process_chain"][0]
        self.__ctx__["chart_processor"] = self.__ctx__["process_chain"][1]
        self.__ctx__["output_processor"] = self.__ctx__["process_chain"][2]

        print(self.__moduleName__ + "初始化完毕，开始执行")

    def dataProcess(self):
        print("处理数据中")
        self.__ctx__["data_processor"].process()

    def chartProcess(self):
        print("图表绘制中")
        self.__ctx__["chart_processor"].process()

    def outputProcess(self):
        print("最后输出中")
        self.__ctx__["output_processor"].process()

    def release(self):
        print("资源已释放")

    def sayHi(self):
        print("hello this is from Monthly Report")


if __name__ == 'main':
    print("hello from Monthly Report")
