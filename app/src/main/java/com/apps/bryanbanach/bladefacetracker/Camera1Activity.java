package com.apps.bryanbanach.bladefacetracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.vuzix.hud.actionmenu.ActionMenuActivity;

import java.io.IOException;

public class Camera1Activity extends ActionMenuActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera1);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setFaceDetectionListener(new FaceDetectionListener());

        // Create our preview view and set it as the content of our activity
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera1_view);
        preview.addView(mPreview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }
}

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera){
        super(context);
        mCamera = camera;

        // Use the SurfaceHolder.Callback to notify us when the surface is created and destroyed
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder){
        // The surface has been created tell the camera where to draw the preview

        try {
            mCamera.setDisplayOrientation(180);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            startFaceDetection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
        // If the preview can change or rotate, take care of that here.
        // Make sure to stop the preview before resizing or reformatting it

        if(mHolder.getSurface() == null){
            // Surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore:  tried to stop a non existant preview
        }

        // set preview size, and make any resize, rotate, or reformatting changes here


        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            startFaceDetection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startFaceDetection(){
        // Try starting face detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only after preview has started
        if(params.getMaxNumDetectedFaces() > 0){
            // Camera supports face detection
            mCamera.startFaceDetection();
        }
    }

}

class FaceDetectionListener implements Camera.FaceDetectionListener{

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if(faces.length > 0){
            Log.d("Face Detection:", "face detected: " + faces.length + " Face 1 Location X: " +
                    faces[0].rect.centerX() + " Y: " + faces[0].rect.centerY());
        }
    }
}
