/*
Gabrielle Albert

2 avril 2017
Activité qui call des Intents de Camera, Rognage, et Gallerie, qui a les fonctions
pour convertir l'image en grayscale bitmap et binary bitmap

Adapté du tutoriel youtube par EDMTDev https://www.youtube.com/watch?v=rYzkv_KuZo4
Images de flaticon.com
 */
package com.example.gabrielle.camactivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends AppCompatActivity {

    File file;
    Uri uri;
    Intent camIntent, galIntent, cropIntent;
    final int RequestPermissionCode=1;
    String fileName = "";

    Button camButton;
    Button galButton;

    ImageView imageView1; //Pour tester la fonction toBinary(bitmapGraysacle)

    Bitmap bitmap; //conversion image-bitmap, bitmap-grayscaleBitmap grayscaleBitmap-binaryBitmap

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView) findViewById(R.id.imageView1); //Pour tester la fonction toBinary(bitmapGraysacle)

        //Button to launch cam intent
        camButton = (Button) findViewById(R.id.camButton);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraOpen();
            }
        });

        //Button to launch gallery intent
        galButton = (Button) findViewById(R.id.galButton);
        galButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryOpen();
            }
        });

        //Permission camera
        int PermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA);
        if(PermissionCheck == PackageManager.PERMISSION_DENIED)
        {
            RequestRuntimePermission();
        }
        Bundle extras;
    }
//Request permission for camera if it's not allowed yet
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
    }
//PERMISSIONS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
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

    private void GalleryOpen()
    {
        galIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galIntent, "Select image from gallery"),2);

    }

    private void CameraOpen()
    {
        camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileName = "file"+String.valueOf(System.currentTimeMillis())+".jpg";
        file = new File(Environment.getExternalStorageDirectory(), fileName);
        uri = Uri.fromFile(file);

        camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        camIntent.putExtra("return data", true);

        startActivityForResult(camIntent, 0);
    }

    /**
     * Crop picture after having taken it with camera or selected it from gallery
     * @param picUri Uri de la photo prise
     */
    private void CropImage(Uri picUri)
    {
        try {

            cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 4);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 1200);
            cropIntent.putExtra("outputY", 300);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent, 1);
        }
        // if the device doesn't support the crop intent (Android 4.3 and older)
        catch (ActivityNotFoundException anfe) {

            Toast toast = Toast.makeText(this, getString(R.string.cropNotSupported), Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode==0 && resultCode==RESULT_OK)
        {
             CropImage(uri);
        }
        else
        {
            if(requestCode==2)
            {
                if(data!=null)
                {
                    uri = data.getData();
                    CropImage(uri);
                }
            }
        }
        if (data != null) //http://stackoverflow.com/questions/14534625/how-to-get-correct-path-after-cropping-the-image
        {
            Bundle extras = data.getExtras();
            bitmap= extras.getParcelable("data");
           // imageView1.setImageBitmap(bitmap);

            bitmap = toGrayscale(bitmap);
            // imageView1.setImageBitmap(bitmap);

            bitmap = toBinary(bitmap);
            //imageView1.setImageBitmap(bitmap); //Pour tester la fonction toBinary(bitmapGraysacle)
        }
    }

    /**
     * Convert picture taken(and cropped) to a grayscale bitmap
     * @param bmpOriginal le bitmap qui sort de la camera/qui vient d'etre rognée
     * @return bitmap converti en grayscale
     */
    public Bitmap toGrayscale(Bitmap bmpOriginal) //http://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Threshold the picture (from grayscale)
     * @param bmpGrayscale bitmap qui vient d'etre converti en grayscale
     * @return bitmap qui a été converti en noir et blanc (image binaire/ threshold)
     */
    public Bitmap toBinary(Bitmap bmpGrayscale) //http://stackoverflow.com/questions/20299264/android-convert-grayscale-to-binary-image
    {
        int width, height, threshold;
        height = bmpGrayscale.getHeight();
        width = bmpGrayscale.getWidth();
        threshold = 156; //Best overall value (tested) with optimal lighting
        Bitmap bmpBinary = Bitmap.createBitmap(bmpGrayscale);

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                int pixel = bmpGrayscale.getPixel(x, y);
                int gray = (int)(Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11);

                //get binary value
                if(gray < threshold){
                    bmpBinary.setPixel(x, y, 0xFF000000);
                } else{
                    bmpBinary.setPixel(x, y, 0xFFFFFFFF);
                }
            }
        }
        return bmpBinary;
    }

    /**
     * @return bitmap noir et blanc / bitmap binaire / threshold bitmap
     */
    private Bitmap getThresholdBitmap()
    {
        return bitmap;
    }

}
