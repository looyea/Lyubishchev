

# 主要程序文件介绍

## __main__.py
主程序入口，不过这个程序依然丑陋，以后会改的


## basecfg.py
负责配置的程序，会从configure.ini文件中读取配置信息，然后进行装配。实际使用的时候，请使用ctx这个上下文环境变量。

配置内容都在configure.ini中，目前功能相对比较简单，根据实际需求进行修改吧。


## global_static.py
主要是负责统计、变换的类，根据实际需求，将需要统计的类，变幻出目标的Pivot table，然后交给相关的程序进行画图处理。
原本是想将这个类，也做画图处理，现在会分开进行。

## README.md
现在阅读的本尊

## ReleaseNote.txt
未来版本考虑要更新的功能，但是不一定严格按照这方面进行。可能中途有了新的想法，然后进行处理的

## test.py

没有用的程序，是开发过程中验证技术使用的测试文件

# Resources目录文档介绍

## template 模板文件夹

这里模板文件夹，主要是为了存放输出、输入用的一些模板使用的。当前因为没有启动模板功能，所以相应的，这些模板都没有实际使用用途。

### 2020.md

当前2020年用的模板，暂时用不到。

## 2020时间日志.xlsx

当前使用的时间日志模板。实际使用的时候，需要填写里面的内容哈。不然就么有意义的。具体的使用，我会用一个使用说明书单独进行说明的。

## midwest_filter.csv

没用的文件，用来验证一些技术试验的时候使用。当前没有用处，可以不用理会。 


