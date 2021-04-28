import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np


class ChartProcessor:

    def __init__(self, ctx = None):
        self.__ctx__ = ctx
        plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
        plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

        sns.set()
        sns.set_style("whitegrid")
        sns.set_palette(sns.color_palette("Accent"))
        sns.set_style({"font.sans-serif": ['Microsoft YaHei', 'SimHei']})  # 显示中文

    def process(self):
        self.plot_df_pie_chart(self.__ctx__['results']['l1pt'])

        l1idx = self.__ctx__['l1_evt_idx']
        l1_values = self.__ctx__['results']['l1_values']
        self.plot_radar_chart(l1idx, l1_values)
        l2idx = self.__ctx__['l2_evt_idx']
        l2_values = self.__ctx__['results']['l2_values']
        self.plot_radar_chart(l2idx, l2_values)

        print("图形处理完毕！")

    def plot_radar_chart(self, titles, data):

        plt.figure(figsize=(10, 6))  # 设置图形大小
        ax = plt.subplot(polar=True)  # 设置图形为极坐标图
        theta = np.linspace(0, 2 * np.pi, len(data))  # 根据index1的数量将圆均分
        # 设置网格，标签
        lines, labels = plt.thetagrids(range(0, 360, int(360 / len(titles))), (titles))
        # 绘制
        ax.plot(theta, data, 'go-', linewidth=2) # 绘制线段
        plt.fill(theta, data, 'g', alpha=0.1)  # 设置颜色与透明度

        ax.spines['polar'].set_visible(False) # 不显示极坐标最外圈的圆
        ax.set_theta_zero_location('N')  # 设置极坐标的起点（即0°）在正北方向，即相当于坐标轴逆时针旋转90°

        # 添加图例和标题
        # plt.legend(labels=(''), loc='lower right', frameon=True, bbox_to_anchor=(1.5, 0.0))  # loc为图例位置

        plt.title("事件分布")
        # plt.savefig(self.__ctx__["module_name"] + "/output/test.png")

        plt.show()

    def plot_df_pie_chart(self, data=None):
        data.plot(kind="pie", y='历时分钟', figsize=(10, 8))
        plt.show()

    '''这里必须是Dataframe才能话'''
    def plot_df_bar_chart(self, data=None):
        data.plot(kind='bar', figsize=(10, 8))
        plt.show()