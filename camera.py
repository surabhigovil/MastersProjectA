import cv2

face_cascade = cv2.CascadeClassifier("/Users/surabhi/PycharmProjects/VideoFaceCapture/haarcascade_frontalface_default.xml")
ds_factor=0.5

mouth_cascade = cv2.CascadeClassifier("/Users/surabhi/PycharmProjects/VideoFaceCapture/haarcascade_mcs_mouth.xml")

if mouth_cascade.empty():
    raise IOError('Unable to load mouth cascade classifier xml file')

class VideoCamera(object):
    def __init__(self):
        self.video = cv2.VideoCapture(0)
    def __del__(self):
        self.video.release()

    def get_frame(self):
        ret, frame = self.video.read()
        frame = cv2.resize(frame, None, fx=ds_factor, fy=ds_factor, interpolation=cv2.INTER_AREA)
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        # face_rect = face_cascade.detectMultiScale(gray,1.3,5)
        # for(x,y,w,h) in face_rect:
        #     cv2.rectangle(frame,(x,y), (x+w,y+h), (0,255,0),2)
        #     break
        # ret, jpeg = cv2.imencode('.jpg', frame)
        mouth_rect = mouth_cascade.detectMultiScale(gray,1.7,11)
        for(x,y,w,h) in mouth_rect:
            cv2.rectangle(frame, (x,y), (x+w,y+h), (0,255,0), 3)
            break
        ret, jpeg = cv2.imencode('.jpg', frame)
        return jpeg.tobytes()