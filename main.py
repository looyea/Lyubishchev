# -*- coding: utf8 -*-
from configparser import ConfigParser, ExtendedInterpolation
import string
import do_statistics as st

# 1. 程序从根目录加载配置
# 1. Main program loads configuration from root folder
config = ConfigParser(interpolation=ExtendedInterpolation())
config.read(r"configure.ini")

# 1.1 设置相关的集合和参数
# 加载替换资源，返回dict
resources = dict()

# 进行统计处理，返回dict
statistics = dict()

# 加载模板
report_template = open(config["Resources"]["report_template"], "r", encoding='utf8')

# 打开待写文件
report_output = open(config["Output"]["output_path"] + config["Output"]["output_file"], "w+", encoding='utf8')
for line in report_template.readlines():
    line = string.Template(line)
    line.substitute(resources)
    line.substitute(statistics)
    report_output.write(line)



# 关闭文档
report_template.close()
report_output.flush()
report_output.close()
