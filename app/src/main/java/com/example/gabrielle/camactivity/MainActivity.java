package com.example.gabrielle.camactivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    File file;
    Uri uri;
    Intent camIntent, galIntent, cropIntent;
    final int RequestPermissionCode=1;
    DisplayMetrics displayMetrics;
    int width, height;

    Button camButton;
    Button galButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        camButton = (Button) findViewById(R.id.camButton);

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraOpen();
            }
        });

        galButton = (Button) findViewById(R.id.galButton);

        galButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryOpen();
            }
        });

        int PermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA);
        if(PermissionCheck == PackageManager.PERMISSION_DENIED)
        {
            RequestRuntimePermission();
        }
    }

    private void RequestRuntimePermission()
    {

        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.CAMERA))
        {
            Toast.makeText(this, "CAMERA permission allow us to access CAMERA app", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.CAMERA},RequestPermissionCode);
        }
        //vid 9:24
    }



    private void GalleryOpen()
    {
        galIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galIntent, "Select image from gallery"),2);

    }

    private void CameraOpen()
    {
        camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "file"+String.valueOf(System.currentTimeMillis())+".jpg");
        uri = Uri.fromFile(file);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        camIntent.putExtra("return data", true);
        startActivityForResult(camIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0 && resultCode==RESULT_OK)
        {
            CropImage();
            ThresholdImage();
        }
        else
        {
            if(requestCode==2)
            {
                if(data!=null)
                {
                    uri = data.getData();
                    CropImage();
                }
            }
        }
    }

    private void ThresholdImage()
    {


    }

    private void CropImage()
    {

        try
        {
            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("outputX", 1024); //taille de l'image a enregistrer
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("aspectX", 4); // ratio 4:1 pour rogner
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return=data", true);

            startActivityForResult(cropIntent, 1);
        }
        catch (ActivityNotFoundException ex)
        {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case RequestPermissionCode:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "Permission cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }
}
