import string


'''
测试字符串相关的问题
'''
values = {'var': 'foo'}

tem = string.Template('''
Variable : ${var}
Escape : $$
Variable in text : ${var}iable
''')

print ('TEMPLATE:', tem.substitute(values))

str = '''
Variable : %(var)s
Escape : %%
Variable in text : %(var)siable
'''

print ('INTERPOLATION:', str % values)