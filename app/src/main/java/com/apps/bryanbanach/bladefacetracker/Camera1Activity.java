package com.apps.bryanbanach.bladefacetracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.vuzix.hud.actionmenu.ActionMenuActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("Deprecated")
public class Camera1Activity extends ActionMenuActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView mImageView;
    private MenuItem takePhotoMenuItem;
    private HandlerThread mBackgroundThread;
    private Handler mHandler;
    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // Do stuff here for the shutter
            //Toast.makeText(getApplicationContext(), "Click!", Toast.LENGTH_SHORT).show();
        }
    };

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFileDir = getDir();
            if(!pictureFileDir.exists() && !pictureFileDir.mkdir()) {
                Log.d("Picture Taken Debug", "Can't create folder to save image");
                return;
            }

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.d("output stream", "photo saved");
                // -- Add to display to imageview -- //
                BitmapFactory.Options scalingOptions = new BitmapFactory.Options();
                scalingOptions.inSampleSize = camera.getParameters().getPictureSize().width / mImageView.getMeasuredWidth();
                final Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length, scalingOptions);
                mImageView.setImageBitmap(bmp);
                mImageView.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                Log.d("FnF", "File not found " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    };

    private File getDir(){
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "cameraDemo");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera1);


    }

    private static boolean hasPermissions(Context context, String... permissions){
        if(context != null && permissions != null){
            for(String permission: permissions){
                if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "Can't run app without permissions", Toast.LENGTH_SHORT).show();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);
        getMenuInflater().inflate(R.menu.photo_menu, menu);
        takePhotoMenuItem = menu.findItem(R.id.take_photo);
        takePhotoMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                takePhotoClick();
                return false;
            }
        });
        return true;
    }

    @Override
    protected boolean alwaysShowActionMenu() {
        super.alwaysShowActionMenu();
        return true;
    }

    @Override
    protected int getDefaultAction() {
        return 1;
    }

    public void takePhotoClick(){
        mCamera.takePicture(mShutterCallback, null, mPictureCallback);
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


        // Create an instance of Camera
        try {

            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                mCamera = getCameraInstance();
                mCamera.setFaceDetectionListener(new FaceDetectionListener());

                // Create our preview view and set it as the content of our activity
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera1_view);
                mImageView = (ImageView) findViewById(R.id.camera1_image_return);
                mImageView.setVisibility(View.INVISIBLE);
                preview.addView(mPreview);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            for(int i = 0; i < faces.length; i++){
                Log.d("Face Detection:", "face detected: " + faces.length + " Face " + i + " Location X: " +
                        faces[0].rect.centerX() + " Y: " + faces[0].rect.centerY());
            }

        }
    }
}
