import matplotlib.pyplot as plt
import seaborn as sns


class ChartProcessor:

    def __init__(self, ctx = None):
        self.__ctx__ = ctx
        plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
        plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号
        # mpl.rcParams['font.sans-serif'] = ['Microsoft YaHei']    # 指定默认字体：解决plot不能显示中文问题
        # mpl.rcParams['axes.unicode_minus'] = False           # 解决保存图像是负号'-'显示为方块的问题

        sns.set()
        sns.set_style("whitegrid")
        sns.set_palette(sns.color_palette("Accent"))
        sns.set_style({"font.sans-serif": ['Microsoft YaHei', 'SimHei']})  # 显示中文

    def process(self, data = None):
        data = self.__ctx__['results']
        # 轮询每个pivot的df
        l1pt = data['l1pt']
        l1pt = l1pt.T
        l1pt.plot(kind="barh", stacked=True, figsize=(10, 8))
        plt.show()
        l1event = data['l1event']
        l1event.plot(kind='bar', figsize=(10, 8))
        plt.show()
        l2event = data['l2event']
        l2event.plot(kind='bar', figsize=(10, 8))
        plt.show()

        print("图形处理完毕！")
