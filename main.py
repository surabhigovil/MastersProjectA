from flask import Flask, render_template, Response
from camera import VideoCamera

app = Flask(__name__)

# # create writer object
# fileName='output.avi'  # change the file name if needed
# imgSize=(640,480)
# frame_per_second=30.0
# writer = cv2.VideoWriter(fileName, cv2.VideoWriter_fourcc(*"MJPG"), frame_per_second,imgSize)
#
# capture = cv2.VideoCapture(0)

# def gen_frames():  # generate frame by frame from camera
#     while(capture.isOpened()):
#         # Capture frame-by-frame
#         success, frame = capture.read()  # read the camera frame
#
#         # Our operations on the frame come here
#         gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
#         gray = cv2.equalizeHist(gray)
#         faces = face_casc.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=3)
#
#         img = frame  # default if face is not found
#
#         if not success:
#             break
#         else:
#             ret, buffer = cv2.imencode('.jpg', frame)
#             for (x, y, w, h) in faces:
#                 roi_gray = gray[y:y + h, x:x + w]
#                 roi_color = frame[y:y + h, x:x + w]
#                 # img=cv2.rectangle(frame, (x, y), (x + w, y + h), color, thickness) # box for face
#                 eyes = eye_casc.detectMultiScale(roi_gray)
#                 for (x_eye, y_eye, w_eye, h_eye) in eyes:
#                     center = (int(x_eye + 0.5 * w_eye), int(y_eye + 0.5 * h_eye))
#                     radius = int(0.3 * (w_eye + h_eye))
#                     img = cv2.circle(roi_color, center, radius, color, thickness)
#                     # img=cv2.circle(frame,center,radius,color,thickness)
#
#                 # Display the resulting image
#             cv2.imshow('Face Detection Harr', img)
#             if cv2.waitKey(1) & 0xFF == ord('q'):  # press q to quit
#                 break
#
#             ret, buffer = cv2.imencode('.jpg', frame)
#             writer.write(frame)  # save the frame into video file
#             cv2.imshow('Video Capture', frame)  # show on the screen
#             if cv2.waitKey(1) & 0xFF == ord('q'):  # press q to quit
#                 break
#             frame = buffer.tobytes()
#             yield (b'--frame\r\n'
#                    b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')  # concat frame one by one and show result
#
#     # When everything done, release the capture
#     capture.release()
#     cv2.destroyAllWindows()
#
# @app.route('/video_feed')
# def video_feed():
#     #Video streaming route. Put this in the src attribute of an img tag
#     return Response(gen_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/')
def index():
    """Video streaming home page."""
    return render_template('index.html')

def gen(camera):
    while True:
        frame = camera.get_frame()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')

# @app.route('/video-feed/', methods=['POST'])
@app.route('/video-feed')
def video_feed():
    return Response(gen(VideoCamera()),
                    mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(debug=True)
