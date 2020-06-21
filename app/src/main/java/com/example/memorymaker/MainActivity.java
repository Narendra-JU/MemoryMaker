package com.example.memorymaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.memorymaker.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mActivityMainBinding;
    File compressedImageFile = null;
    ImageView image;
    int choice=0;
    private static final int SELECT_PICTURE = 1;
    Bitmap mBitmap=null;
    CardView mCardView;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        mActivityMainBinding = DataBindingUtil
                .setContentView(this, R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mCardView=findViewById(R.id.cardview);








        List<String> testDeviceIds = Arrays.asList("8284E3A4DB504809B8690D53DFF332E9");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);















        mActivityMainBinding.dialogAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.create(MainActivity.this).folderMode(true).single().start();
                choice=1;
            }
        });

        mActivityMainBinding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice=2;

                ImagePicker.create(MainActivity.this).folderMode(true).single().start();

            }
        });

        mActivityMainBinding.fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExternalStorageWritable()){
                    //shareImageUri(saveImageExternal(getBitmapFromView(mCardView)));
                    View view=findViewById(R.id.cardview);
                    Bitmap bitmap=getBitmapFromView(view);
                    Uri uri=saveImageExternal(bitmap);
                    shareImageUri(uri);
                }
            }
        });




    }


    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        ImageView image = mActivityMainBinding.image;
        CircleImageView cimage=mActivityMainBinding.dialogAvatar;
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image selectedImage = ImagePicker.getFirstImageOrNull(data);

            try {
                compressedImageFile = new Compressor(this)
                        .setQuality(75)
                        .compressToFile(new File(selectedImage.getPath()));


                if(choice==2) {
                    Picasso.get().load(new File(selectedImage.getPath())).placeholder(R.drawable.memoriesimage).into(image);
                }
                else if(choice==1){
                    Picasso.get().load(new File(selectedImage.getPath())).placeholder(R.drawable.memoriesimage).into(cimage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri saveImageExternal(Bitmap image) {
        //TODO - Should be processed in another thread
        Uri uri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "to-share.png");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.close();
            uri = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
        } catch (IOException e) {
            Log.d("Tag", "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void shareImageUri(Uri uri){
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "to-share.png");
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        /*Uri apkURI = FileProvider.getUriForFile(
                MainActivity.this,
                MainActivity.this.getApplicationContext()
                        .getPackageName() + ".provider",file);
        intent.setDataAndType(apkURI, "image/png");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);*/


        startActivity(intent);
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

}
