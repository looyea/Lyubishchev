from configparser import ConfigParser, ExtendedInterpolation
import pandas as pd

__ctx__ = dict()
__moduleName__ = "MonthlyReport"


def init(data=None):
    print(__moduleName__ + "初始化……")
    # 配置加载
    __loadConfig__()

    # 数据加载
    __loadData__(data)

    # 处理链加载
    __loadTaskProcessors__()

    print(__moduleName__ + "初始化完毕，开始执行")


def __loadConfig__():
    ctx = ConfigParser(interpolation=ExtendedInterpolation())
    ctx.read(__moduleName__ + "/" + __moduleName__ + ".ini", encoding="utf-8")
    global __ctx__

    for key in ctx[__moduleName__].keys():
        __ctx__[key] = ctx[__moduleName__][key]
        # print(key + ":" + __ctx__[key])


def __loadData__(data):
    global __ctx__
    # read data from data_file if not given
    if data is None:
        data = pd.read_excel(
            __ctx__['data_source_path'],
            sheet_name=__ctx__["current_month"],
            index_col=None
        )
    __ctx__['data'] = data


def __loadTaskProcessors__():
    # 根据处理配置处理具体处理器加载, 目前设定就是3个：数据、图形、输出
    global __ctx__
    task_processors_names = __ctx__["task_chain"].split(",")
    __ctx__["process_chain"] = list()
    for name in task_processors_names:
        processor = __import__(__moduleName__ + "." + name, fromlist = True)
        __ctx__["process_chain"].append(processor)

    __ctx__["data_processor"] = __ctx__["process_chain"][0]
    __ctx__["chart_processor"] = __ctx__["process_chain"][1]
    __ctx__["output_processor"] = __ctx__["process_chain"][2]



def dataProcess():
    print("处理数据中")
    __ctx__["data_processor"].process(__ctx__['data'])


def chartProcess():
    print("图表绘制中")
    __ctx__["chart_processor"].process(__ctx__['data'])

def outputProcess():
    print("最后输出中")
    __ctx__["output_processor"].process(__ctx__['data'])


def release():
    print("资源已释放")
    print(__moduleName__ + "任务执行完毕！")


def sayHi():
    print("hello this is from Monthly Report")



if __name__ == 'main':
    print("hello from Monthly Report")