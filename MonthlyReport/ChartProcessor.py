import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import math


class ChartProcessor:

    def __init__(self, ctx=None):
        self.__ctx__ = ctx
        plt.rcParams['font.sans-serif'] = ['Microsoft YaHei']  # 用来正常显示中文标签
        plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

        sns.set()
        sns.set_style("whitegrid")
        sns.set_palette(sns.color_palette("Accent"))
        sns.set_style({"font.sans-serif": ['Microsoft YaHei', 'SimHei']})  # 显示中文

    def process(self):
        self.plot_df_bar_chart(self.__ctx__['results']['time_base'])

        l1idx = self.__ctx__['l1_evt_idx']
        l1_values = self.__ctx__['results']['l1_values']
        self.plot_polygon_radar_chart(l1idx, l1_values)
        l2idx = self.__ctx__['l2_evt_idx']
        l2_values = self.__ctx__['results']['l2_values']
        self.plot_polygon_radar_chart(l2idx, l2_values)

        print("图形处理完毕！")

    def plot_radar_chart(self, titles, data):

        plt.figure(figsize=(10, 6))  # 设置图形大小
        ax = plt.subplot(polar=True)  # 设置图形为极坐标图
        ax.set_theta_zero_location('N')  # 设置极坐标的起点（即0°）在正北方向，即相当于坐标轴逆时针旋转90°
        plt.thetagrids(range(0, 360, int(360 / len(titles))), titles)  # 设置网格，标签

        # 绘制
        theta = np.linspace(0, 2 * np.pi, len(data[[*data][0]]))  # 根据index1的数量将圆均分
        for key in data.keys():
            ax.plot(theta, data[key], 'o-', linewidth=1)  # 绘制线段
            plt.fill(theta, data[key], alpha=0.1)  # 设置颜色与透明度

        ax.spines['polar'].set_visible(False)  # 不显示极坐标最外圈的圆

        # 添加图例和标题 loc为图例位置
        plt.legend(labels=(self.__ctx__['month_to_do']), loc='lower right', frameon=True, bbox_to_anchor=(1.5, 0.0))

        plt.title("事件分布")
        # plt.savefig(self.__ctx__["module_name"] + "/output/test.png")

        plt.show()

    def plot_polygon_radar_chart(self, titles, data):
        plt.figure(figsize=(10, 6))  # 设置图形大小
        ax = plt.subplot(polar=True)  # 设置图形为极坐标图

        # 设置网格，标签
        plt.thetagrids(range(0, 360, int(360 / len(titles))), titles)
        theta = np.linspace(0, 2 * np.pi, len(data[[*data][0]]))  # 根据index1的数量将圆均分
        max_value = 0  # 寻找数值中最大的那个

        # 绘制图形， 这里必须放到前面，不然legend就不对
        for key in data.keys():
            # 绘制图形
            max_value = max(max_value, max(data[key]))
            ax.plot(theta, data[key], 'o-', linewidth=1)  # 绘制线段
            plt.fill(theta, data[key], alpha=0.1)  # 设置颜色与透明度
            # 绘制数据标签
            for a, b in zip(theta, data[key]):
                ax.text(a, b + 5, '%.00f' % b, ha='center', va='center', fontsize=8, color='b')

        print(max_value)
        # 绘制刻度线 先确定刻度线最大值外围情况
        for i in range(0, 10000, 200):
            if max_value < i:
                max_value = i
                break

        print(max_value)
        # 再确定一下具体的Step的数值是多少了，之后绘制就可以根据情况来了
        step_value = math.floor(max_value / len(titles))
        print(step_value)
        for j in np.arange(0, max_value, step_value):
            ax.plot(theta, len(theta) * [j], '--', lw=0.5, color='black')
        # 绘制从中心到四周的轴线
        for j in range(len(theta)):
            ax.plot([theta[j], theta[j]], [0, 1000], '--', lw=0.5, color='black')

        # 图形设置
        ax.set_theta_zero_location('N')  # 设置极坐标的起点（即0°）在正北方向，即相当于坐标轴逆时针旋转90°
        ax.set_thetagrids(theta * 180 / np.pi, titles + titles[:1])  # 绘制外层标签
        ax.spines['polar'].set_visible(False)  # 隐藏最外圈的圆
        ax.grid(False)  # 隐藏圆形网格线
        ax.set_theta_zero_location('N')  # 角度专项正北
        ax.set_rlim(0, max_value)  # 设置半径上面的刻度
        ax.set_rlabel_position(0)  # 设置半径标签偏转
        # 添加图例和标题 loc为图例位置
        plt.legend(labels=(self.__ctx__['month_to_do']), loc='lower right', frameon=True, bbox_to_anchor=(1.5, 0.0))
        plt.title("事件分布")
        # plt.savefig(self.__ctx__["module_name"] + "/output/test.png")

        plt.show()

    def plot_df_pie_chart(self, data=None):
        data.plot(kind="pie", y=self.__ctx__['sum_tag'], figsize=(10, 8))
        plt.show()

    # 这里必须是Dataframe才能用
    def plot_df_bar_chart(self, data=None):
        ax = data.plot(kind='bar', figsize=(10, 8))
        for x, idx in zip(ax.get_xticks(), data.index):
            for val in data.loc[idx]:
                ax.text(x, val, val, ha="center", va="center")

        plt.show()

