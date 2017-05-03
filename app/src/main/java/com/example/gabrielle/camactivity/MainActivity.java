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
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* A mettre dans le guide d'utilisation
Ne pas utiliser de crayons / stylos de couleur (juste noir et gris foncé - plomb)
Éviter d'écrire pale au crayon plomb

Luminosité optimale requise

Éviter de couvrir l'équation avec l'ombre du telephone (VRM IMPORTANT)

Favoriser la lumière à la place de la proximité (Ça fait vrm toute la différence)

 */


public class MainActivity extends AppCompatActivity
{

    File file;
    Uri uri;
    Intent camIntent, galIntent, cropIntent;
    final int RequestPermissionCode=1;
    String fileName = "";

    Boolean ruledPaper = false;
    Button camButton;
    Button galButton;

    double camRes = 0; //To store the camera resolution

    ImageView imageView1; //Pour tester la fonction toBinary(bitmapGraysacle)

    Bitmap bitmap; //conversion image-bitmap, bitmap-grayscaleBitmap grayscaleBitmap-binaryBitmap

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


/*      //A remettre dans le code final, verifie preferences de papier pour avoir une valeur threshold optimale
        Intent i = getIntent();
        if(i.getBooleanExtra("FEUILLE", true))
        {
            ruledPaper = false;
        }
        else
        {
            if(i.getBooleanExtra("FEUILLE", false))
            {
                ruledPaper = true;
            }
        }
*/
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

//Code to check camera resolution in megapixels
        //http://stackoverflow.com/questions/19463858/how-to-get-front-and-back-cameras-megapixel-that-is-designed-for-android-device

           Camera camera=Camera.open(0);
           android.hardware.Camera.Parameters params = camera.getParameters();
           List sizes = params.getSupportedPictureSizes();
           Camera.Size  result = null;

           ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
           ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

           for (int i=0;i<sizes.size();i++){
               result = (Camera.Size) sizes.get(i);
               arrayListForWidth.add(result.width);
               arrayListForHeight.add(result.height);
           }
           if(arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0)
           {
               camRes = ((Collections.max(arrayListForWidth)) * (Collections.max(arrayListForHeight))) / 1024000;
           }
           camera.release();
           arrayListForWidth.clear();
           arrayListForHeight.clear();
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
        }
    }
 //When you get back from cropping and choosingtaking picture
    @Override
    protected void onResume() {
        super.onResume();
        if(bitmap != null)
        {
            bitmap = toGrayscale(bitmap);
            bitmap = toBinary(bitmap);
            imageView1.setImageBitmap(bitmap); //Pour tester la fonction toBinary(bitmapGraysacle)
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
        int width, height, threshold; //threshold = minimum value a pixel needs to be black (kept as text)
        height = bmpGrayscale.getHeight();
        width = bmpGrayscale.getWidth();

        if(camRes<=8 && ruledPaper == false) //older cameras (8MP and less) user chose blank paper as default
        {
            threshold = 127; //Tested w/GS3 8MP camera
        }
        else
        {
            if(camRes<=8 && ruledPaper == true) //older cameras (8MP and less) user chose ruled paper as default
            {
                threshold = 112; //Tested w/GS3 8MP camera
            }

            else //Cameras over 8MP
            {
                threshold = 156; //Best overall value with good lighting(tested w/ GS6 16MP camera)
            }
        }
        Bitmap bmpBinary = Bitmap.createBitmap(bmpGrayscale);

        for(int x = 0; x < width; ++x)
        {
            for(int y = 0; y < height; ++y)
            {
                // get one pixel color
                int pixel = bmpGrayscale.getPixel(x, y);
                int gray = (int)(Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11);

                //get binary value
                if(gray < threshold)
                {
                    bmpBinary.setPixel(x, y, 0xFF000000); //make pixel black
                } else
                {
                    bmpBinary.setPixel(x, y, 0xFFFFFFFF); //make pixel white
                }
            }
        }
        return bmpBinary;
    }
}
