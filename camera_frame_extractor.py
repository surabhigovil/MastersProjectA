import cv2
import os

face_cascade = cv2.CascadeClassifier("/Users/surabhi/PycharmProjects/VideoFaceCapture/haarcascade_frontalface_default.xml")
ds_factor=0.5

dirFace = 'cropped_face'

# Create if there is no cropped face directory
if not os.path.exists(dirFace):
    os.mkdir(dirFace)
    print("Directory " , dirFace ,  " Created ")
else:
    print("Directory " , dirFace ,  " has found.")

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

        # detectfaces
        faces = face_cascade.detectMultiScale(
            frame,  # stream
            scaleFactor=1.10,  # change these parameters to improve your video processing performance
            minNeighbors=20,
            minSize=(30, 30)  # min image detection size
        )

        # Draw rectangles around each face
        for (x, y, w, h) in faces:
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), thickness=2)
            # saving faces according to detected coordinates
            sub_face = frame[y:y + h, x:x + w]
            FaceFileName = "cropped_face/face_" + str(y + x) + ".jpg"  # folder path and random name image
            cv2.imwrite(FaceFileName, sub_face)
        # Video Window
        #cv2.imshow('Video Stream', frame)

        mouth_rect = mouth_cascade.detectMultiScale(gray,1.7,11)
        for(x,y,w,h) in mouth_rect:
            cv2.rectangle(frame, (x,y), (x+w,y+h), (0,255,0), 3)
            break
        ret, jpeg = cv2.imencode('.jpg', frame)
        return jpeg.tobytes()
