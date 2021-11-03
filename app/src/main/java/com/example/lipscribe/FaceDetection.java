package com.example.lipscribe;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class FaceDetection {
    private CascadeClassifier facedetector;
    private Interpreter interpreter,interpreter2;
    File file;

    public FaceDetection(AssetManager assetManager, Context context, String modelPath) {
        Log.i("Face detect","constructor");
        Interpreter.Options options=new Interpreter.Options();
        GpuDelegate gpuDelegate = new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(4); // change number of thread according to your phone

//        load tflite modelPath
//        try{
//            interpreter2=new Interpreter(loadModelFile(assetManager,"model_2.tflite"),options);
//        }
//        catch(IOException e){
//            e.printStackTrace();
//        }

//         load CNN model
        try {
            interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try
        {
            InputStream i = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File dir = context.getDir("cascade", Context.MODE_PRIVATE);
            file = new File(dir, "haarcascade_frontalface_alt2.xml");
            FileOutputStream o = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytes;
            while ((bytes = i.read(buffer)) != -1) {
                o.write(buffer, 0, bytes);
            }
            i.close();
            o.close();
            facedetector = new CascadeClassifier(file.getAbsolutePath());
            if (facedetector.empty()) {
                facedetector = null;
                Log.i("face detector","is null");
            } else {
                dir.delete();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public Mat mouthExtraction(Mat rgba,Mat gray)
    {
        Mat a=rgba.t();
        Mat b=gray.t();
        Core.flip(a,rgba,1);
        a.release();
        Core.flip(b,gray,1);
        b.release();
        MatOfRect facedetection = new MatOfRect();
        Log.d("debug","face detection");
        facedetector.detectMultiScale(gray, facedetection);
        for (Rect rect : facedetection.toArray()) {
            Log.i("rectangle","rectangle");
            Log.i("rectangle", String.valueOf(rect.x));
            Imgproc.rectangle(rgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0),3);
            // now convert cropped gray scale face image to bitmap
            Bitmap bitmap=null;
            Rect roi=new Rect((int)rect.tl().x,(int)rect.tl().y,(int)rect.br().x-(int)rect.tl().x,(int)rect.br().y-(int)rect.tl().y);
            Mat cropped=new Mat(gray,roi);
            // cropped rgba image
            Mat cropped_rgba=new Mat(rgba,roi);
            int inputsize = 150;
            Size s=new Size(inputsize, inputsize);
            Mat resizeimg=new Mat();
            Imgproc.resize(cropped,resizeimg,s,0,0,Imgproc.INTER_CUBIC);
            bitmap=Bitmap.createBitmap(cropped.cols(),cropped.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped,bitmap);
            // define height and width of cropped bitmap
            int c_height=bitmap.getHeight();
            int c_width=bitmap.getWidth();
//                // now convert cropped grayscale bitmap to buffer byte
//                // before that scale it to (96,96)
//                // input size of interpreter is 96
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap, inputsize, inputsize,false);
            ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);
//                // now define output
            float[][] result=new float[1][136];// total 30 coordinate
//                // predict
            Log.d("MainActivity","loaded");
            //Log.d("byteBuffe", String.valueOf(byteBuffer));
            Log.d("result", String.valueOf(result));
//            Log.d("inter", String.valueOf(interpreter));
            interpreter.run(byteBuffer,result);
               // height,width of cropped face is different from input size of Interpreter
            // we have to scale each key point co-ordinate for cropped face
            float x_scale=((float)c_width)/((float) inputsize);
            float y_scale=((float)c_height)/((float) inputsize); // or you can divide it with INPUT_SIZE
            float x1=(float) Array.get(Array.get(result,0),96)-20;
            float y1=(float) Array.get(Array.get(result,0),101)-20;
            float x2=(float) Array.get(Array.get(result,0),108)+20;
            float y2=(float) Array.get(Array.get(result,0),117)+20;

            Imgproc.rectangle(cropped_rgba , new Point(x1*x_scale,y1*y_scale), new Point(x2*x_scale, y2*y_scale), new Scalar(255, 0, 0),1);
//                // loop through each key point
            for (int j=0;j<136;j=j+2){
                // now define x,y co-ordinate
                // every even value is x co-ordinate
                // every odd value is y co-ordinate
                float x_val=(float) Array.get(Array.get(result,0),j);
                float y_val=(float)Array.get(Array.get(result,0),j+1);


                // draw circle around x,y
                // draw on cropped_rgb not on cropped
                //              input/output     center                                  radius        color                fill circle
                Imgproc.circle(cropped_rgba,new Point(x_val*x_scale,y_val*y_scale),3,new Scalar(0,255,0,255),-1);
                Imgproc.putText(cropped_rgba, String.valueOf(j),new Point(x_val*x_scale,y_val*y_scale),1,1,new Scalar(0,255,0,255),1);
            }

//             replace cropped_rgba with original face on mat_image
            cropped_rgba.copyTo(new Mat(rgba,roi));
        }
        Mat c=rgba.t();
        Core.flip(c,rgba,0);
        c.release();
        return rgba;
    }
//    public String prediction(ArrayList<Mat> images)
//    {
//        String p = null;
////        Bitmap bitmap=null;
////        bitmap=Bitmap.createBitmap(cropped.cols(),cropped.rows(),Bitmap.Config.ARGB_8888);
////        Utils.matToBitmap(images.get(0),bitmap);
////        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap, inputsize, inputsize,false);
////        ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);
//
//        interpreter2.run(images,p);
//        return p;
//    }
    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int inputSize=150;
        int quant=1;
        if(quant==0)
        {
            byteBuffer=ByteBuffer.allocateDirect(3*1*inputSize*inputSize);
        }
        else
        {
            byteBuffer=ByteBuffer.allocateDirect(4*1*inputSize*inputSize*3);
        }

        byteBuffer.order(ByteOrder.nativeOrder());
        int pixel=0;
        int [] intValues=new int [inputSize*inputSize];
        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());

        for (int i=0;i<inputSize;++i){
            for(int j=0;j<inputSize;++j){
                final int val= intValues[pixel++];
                byteBuffer.putFloat((((val>>16) & 0xFF))/255.0f);// scaling it from 0-255 to 0-1
                byteBuffer.putFloat((((val>>8) & 0xFF))/255.0f);
                byteBuffer.putFloat((((val & 0xFF)& 0xFF))/255.0f);
            }
        }
        return  byteBuffer;

    }
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // description of file
        Log.i("Model loading",modelPath);
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
}
