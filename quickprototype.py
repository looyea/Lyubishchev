import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

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
                     index=["时间分类","事件"],
                     # columns=["并行"],
                     values="历时分钟",
                     aggfunc=[np.sum],
                      fill_value=0, margins=True)
fig = plt.plot()

print(pt.values)


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