from configparser import ConfigParser, ExtendedInterpolation
import pandas as pd
import yaml


class MonthlyReport():

    def __init__(self, data=None):
        self.__module_name__ = "MonthlyReport"
        self.__ctx__ = dict()
        self.__data__ = data

        print(self.__module_name__ + "初始化……")
        # 配置加载
        with open(self.__module_name__ + "/" + self.__module_name__ + ".yaml", 'rb') as stream:
            try:
                ctx = yaml.safe_load(stream)
                for key in ctx.keys():
                    self.__ctx__[key] = ctx[key]
                stream.close()
            except yaml.YAMLError as exc:
                print(exc)


        # 数据加载
        if data is None:
            data = pd.read_excel(
                self.__ctx__['data_source_path'],
                sheet_name=None,
                index_col=None
            )
        self.__ctx__['data'] = data

        # 处理链加载
        task_processors_names = self.__ctx__["task_chain"]
        self.__ctx__["process_chain"] = list()
        for name in task_processors_names:
            processor = __import__(self.__module_name__ + "." + name, fromlist="True")
            cls = getattr(processor, name)
            self.__ctx__["process_chain"].append(cls(self.__ctx__))

        self.__ctx__["data_processor"] = self.__ctx__["process_chain"][0]
        self.__ctx__["chart_processor"] = self.__ctx__["process_chain"][1]
        self.__ctx__["output_processor"] = self.__ctx__["process_chain"][2]

        print(self.__module_name__ + "初始化完毕，开始执行")

    def data_process(self):
        print("处理数据中")
        self.__ctx__["data_processor"].process()

    def chart_process(self):
        print("图表绘制中")
        self.__ctx__["chart_processor"].process()

    def output_process(self):
        print("最后输出中")
        self.__ctx__["output_processor"].process()

    def release(self):
        print("资源已释放")


if __name__ == 'main':
    print("hello from Monthly Report")
