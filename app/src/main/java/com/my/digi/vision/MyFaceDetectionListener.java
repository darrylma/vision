package com.my.digi.vision;

import android.hardware.Camera;
import android.hardware.camera2.*;
import android.hardware.Camera.Face;
import android.util.Log;

class MyFaceDetectionListener implements Camera.FaceDetectionListener {

	@Override
    public void onFaceDetection(Face[] faces, Camera camera) {
        if (faces.length > 0){
            Log.d("FaceDetection", "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    "Y: " + faces[0].rect.centerY() );
        }
    }
}
