# -.- encoding=utf-8 -.-
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

file_name = "Resources\\2020TimeTable.xlsx"
sheet_names = [x for x in range(1, 13)]
tag_names = ['标签信息']


xlsx = pd.ExcelFile(file_name)
# print(xlsx.sheet_names)
# pd.read_Excel(xls, 'Sheet2')

df = xlsx.parse("8")
# print(df.keys())
# print(len(df))

# 获取历时分钟数
pt = pd.pivot_table(df,
                     index=["时间分类"],
                     # columns=["并行"],
                     values="历时分钟",
                     aggfunc=[np.sum],
                      fill_value=0, margins=True)

def make_autopct(values):
    def my_autopct(pct):
        total = sum(values)
        val = int(round(pct*total/100.0))
        # 同时显示数值和占比的饼图
        return '{p:.2f}%  ({v:d})'.format(p=pct,v=val)
    return my_autopct

# print(pt)
pt = pt.drop(index=['All'])
pt = pt['sum']
#print(pt.index.values)
# print(pt['sum']['历时分钟'])
# print(type(pt['sum']['历时分钟']))
print(pt['历时分钟'].values)
print(pt['历时分钟'].index.values)


def auto_value(val):
    print(val)
    return val

#
plt.rcParams['font.sans-serif']=['SimHei']
pt.plot(kind='pie', subplots=True, figsize=(8, 8), autopct=auto_value)
plt.title("分类时间表")
plt.ylabel("")
plt.show()



# 将内容转换成字典，批量读取
# sheet_to_df_map = {}
# for sheet_name in xls.sheet_names:
#     sheet_to_df_map[sheet_name] = xls.parse(sheet_name)
# 按照序号选择Sheet页面
# sheet1 = xls.parse(0)

# 读取csv相关的方式，以后会用到
# import d6tstack
# c = d6tstack.convert_xls.XLStoCSVMultiSheet('multisheet.xlsx')
# c.convert_all() # ['multisheet-Sheet1.csv','multisheet-Sheet2.csv']