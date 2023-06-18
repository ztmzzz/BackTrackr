FROM registry.baidubce.com/paddlepaddle/paddle:2.4.2
LABEL authors="ztmzzz"
EXPOSE 8866
RUN pip install --no-cache-dir paddlehub==2.3.1 shapely pyclipper
RUN hub install ch_pp-ocrv3_det==1.1.0
RUN hub install ch_pp-ocrv3==1.2.0
ENTRYPOINT ["hub","serving","start","-m","ch_pp-ocrv3","--use_multiprocess"]