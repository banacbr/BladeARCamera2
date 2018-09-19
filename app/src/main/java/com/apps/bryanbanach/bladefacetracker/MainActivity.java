package com.apps.bryanbanach.bladefacetracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.vuzix.hud.actionmenu.ActionMenuActivity;

public class MainActivity extends ActionMenuActivity {

    private MenuItem HelloMenuItem;
    private MenuItem VuzixMenuItem;
    private MenuItem BladeMenuItem;
    private TextView mainText;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected boolean onCreateActionMenu(Menu menu){
        super.onCreateActionMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);

        HelloMenuItem = menu.findItem(R.id.item1);
        VuzixMenuItem = menu.findItem(R.id.item2);
        BladeMenuItem = menu.findItem(R.id.item3);
        mainText = findViewById(R.id.mainTextView);

        updateMenuItems();

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                //close the app
                Toast.makeText(this, "Need access to camera", Toast.LENGTH_SHORT);
                finish();
            }
        }
    }

    @Override
    protected boolean alwaysShowActionMenu(){
        return false;
    }

    private void updateMenuItems(){
        if (HelloMenuItem == null) {
            return;
        }

        VuzixMenuItem.setEnabled(false);
        BladeMenuItem.setEnabled(false);
    }

    //Action menu click events
    public void showHello(MenuItem item){
        showToast("Hello World!");
        mainText.setText("Hello world");
        VuzixMenuItem.setEnabled(true);
        BladeMenuItem.setEnabled(true);
    }

    public void showVuzix(MenuItem item){
        showToast("Vuzix");
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void showBlade(MenuItem item){
        showToast("Blade");
        mainText.setText("Blade");
    }

    private void showToast(final String text){
        final Activity activity = this;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
