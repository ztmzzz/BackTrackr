import base64
import json

import cv2
import numpy as np
from flask import Flask, request, Response

import utility
from predict_system import TextSystem

app = Flask(__name__)

args = utility.parse_args()
args.det_limit_side_len = 2560
args.det_model_dir = "./model/ch_PP-OCRv3_det_infer/"
args.rec_model_dir = "./model/ch_PP-OCRv3_rec_infer/"
args.cls_model_dir = "./model/ch_ppocr_mobile_v2.0_cls_infer"
args.use_angle_cls = True
text_sys = TextSystem(args)


@app.route('/ocr', methods=['POST'])
def ocr():
    data = request.get_json()
    img_data = base64.b64decode(data['images'])
    nparr = np.frombuffer(img_data, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    dt_boxes, rec_res = text_sys(img)
    result = []
    for idx, box in enumerate(dt_boxes):
        temp = {"confidence": rec_res[idx][1], "text": rec_res[idx][0], "text_box_position": box.tolist()}
        result.append(temp)

    response = Response(
        response=json.dumps({'data': result}, ensure_ascii=False),
        content_type="application/json; charset=utf-8",
        status=200
    )
    return response


if __name__ == '__main__':
    app.run(host='localhost', port=8866)
