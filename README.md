# 简介

这是一个类似Rewind Ai的软件,目的是记录平时的电脑操作,以便在忘记某些事情的时候根据关键词找回自己的记忆

目前这个项目还在开发中,现在算是一个堪堪可用的版本

这个readme也是临时的,后续会优化修改

这个项目的逻辑是每个几秒进行截图,对截图ocr后存入数据库,每隔一天将所有截图生成视频.

使用时根据关键词搜索数据库,然后在视频相应位置有红框标注

# 使用方法
目前用的OCR是paddle ocr,必须使用GPU来部署,因为paddle ocr速度比较慢,跟不上截图的间隔

需要安装paddlepaddle-gpu,详情看(https://www.paddlepaddle.org.cn/),注意安装对应的cuda版本和cudnn

pip install paddlepaddle-gpu==2.5.0.post118 -f https://www.paddlepaddle.org.cn/whl/windows/mkl/avx/stable.html


1. 在PaddleOCR文件夹下有一个requirements.txt,安装好后运行PaddleOCR/server.py
2. 启动springboot项目作为后端
3. gui文件夹下是前端界面,运行electron:serve可以得到应用
4. 发送post请求localhost:8080/startScreenshot,开始截图
5. 发送post请求localhost:8080/stopScreenshot,停止截图
6. 发送post请求localhost:8080/startOCR,开始ocr
7. 发送post请求localhost:8080/stopOCR,停止ocr

后面几步很糙,后续会优化,改成应用的按钮