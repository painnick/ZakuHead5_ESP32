import json
import socket
import time
from urllib import response
import cv2
from cv2 import ROTATE_180
import mediapipe as mp
import urllib.request
from urllib.error import HTTPError, URLError
import numpy as np
import logging

logger = logging.getLogger("main")
logger.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
stream_handler = logging.StreamHandler()
stream_handler.setFormatter(formatter)
logger.addHandler(stream_handler)

mp_face_detection = mp.solutions.face_detection
mp_drawing = mp.solutions.drawing_utils

host = 'http://192.168.0.29'

led_state = False

face_found = time.time()
face_lost = time.time()
direction = 'none'
last_angle = 90


def parseAngle(res):
  json_string = res.read().decode()
  # logger.debug(json_string)
  res_obj = json.loads(json_string)
  angle = res_obj.get("angle")
  # logger.debug(angle)
  return angle


# For webcam input:
with mp_face_detection.FaceDetection(
    model_selection=0, min_detection_confidence=0.6) as face_detection:
  while True:
    try:
      img_resp = urllib.request.urlopen(url=host + '/capture', timeout=1)
    except (HTTPError, URLError) as error:
      logging.error('Data not retrieved because %s', error)
      continue
    except ConnectionResetError:
      logger.error('Connection Reset!')
      continue

    try:
      imgnp = np.array(bytearray(img_resp.read()),dtype=np.uint8)
    except socket.timeout:
      logger.error('Timeout!')
      continue

    image = cv2.imdecode(imgnp,-1)

    # To improve performance, optionally mark the image as not writeable to
    # pass by reference.
    image.flags.writeable = False
    image = cv2.rotate(image, ROTATE_180)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    results = face_detection.process(image)

    # Draw the face detection annotations on the image.
    image.flags.writeable = True
    image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
    if results.detections: # Found
      face_found = time.time()
      # LED가 꺼져있으면 켠다. 매번 호출하면 반응이 느림
      if led_state == False:
        logger.debug('LED On')
        urllib.request.urlopen(host + '/led?bright=10')
        led_state = True

      for detection in results.detections:
        # 모니터링 화면에 얼굴 영역을 표시
        box = detection.location_data.relative_bounding_box
        center = box.xmin + (box.width / 2)
        mp_drawing.draw_detection(image, detection)
        # 화면 중심에 얼굴을 배치하기 위해 카메라를 조금씩 이동
        if 0.4 > center or center > 0.6:
          if center > 0.5:
            direction = 'right'
            last_angle = parseAngle(urllib.request.urlopen(host + '/servo?dir=%s&step=%d&found=true' %(direction, 10)))
            # logger.debug(direction)
          else:
            direction = 'left'
            last_angle = parseAngle(urllib.request.urlopen(host + '/servo?dir=%s&step=%d&found=true' %(direction, 10)))
            # logger.debug(direction)

      # face_lost 초기화
      face_lost = time.time()
    else: # Lost
      current_time = time.time()
      # 일정 시간동안 얼굴을 찾지 못하면 놓친 것으로 판단
      if current_time - face_lost > 2:
        face_lost = time.time()
        if led_state == True:
          urllib.request.urlopen(host + '/led?bright=0')
          logger.debug('LED Off')
          led_state = False
        # 얼굴을 놓치면 기존 이동 방향으로 한 번 더 이동
        if 20 < last_angle and last_angle < 160:
          last_angle = parseAngle(urllib.request.urlopen(host + '/servo?dir=%s&step=%d&found=false' %(direction, 30)))
          logger.debug(direction)

    # Flip the image horizontally for a selfie-view display.
    cv2.imshow('MediaPipe Face Detection', cv2.flip(image, 1))
    if cv2.waitKey(5) & 0xFF == 27:
      break
