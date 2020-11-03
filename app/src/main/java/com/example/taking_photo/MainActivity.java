package com.example.taking_photo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button button;
    Button test; 
    TextView textView; 
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ActionBar actionBar;
    Bitmap imageBitmap;
    //permissomn
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STROAGE_REQUEST_CODE=200;
    //IMAGE PIC CONTAINTS
    private static final int IMAGE_PIC_CAMERA_CODE=300;
    private static final int IMAGE_PIC_GALARY_CODE=400;
    //array permission
    private String[] camerapermission;
    private String[] stroagepermisson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.text_display);
        test=findViewById(R.id.test);
       actionBar=getSupportActionBar();
     actionBar.setTitle("Detect Text");
        //enable background
        actionBar.setDisplayHomeAsUpEnabled(true);
       actionBar.setDisplayShowHomeEnabled(true);
        //permission
        camerapermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        stroagepermisson=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        imageView=findViewById(R.id.iamge);
        button=findViewById(R.id.capture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();

            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextFromImage();
                textView.setText(null);

            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);

            }
        });
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
             imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }
    public void getTextFromImage()
    {
        FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector=
                FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error : ",e.getMessage());
            }
        });


      }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        TextRecognizer textRecognizer=new TextRecognizer.Builder(getApplicationContext()).build();
        List<FirebaseVisionText.Block>blockList=firebaseVisionText.getBlocks();
        if (blockList.size()==0) {
            Toast.makeText(this, "No Image Found..Opps!!!", Toast.LENGTH_SHORT).show();
        }
        else {
           /* for (FirebaseVisionText.Block block:firebaseVisionText.getBlocks() ) {
                String text=block.getText();
                textView.setText(text);
            }*/
            Frame frame=new Frame.Builder().setBitmap(imageBitmap).build();
            SparseArray<TextBlock>items=textRecognizer.detect(frame);
            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<items.size();++i) {
                TextBlock myitem=items.valueAt(i);
                stringBuilder.append(myitem.getValue());
                stringBuilder.append("\n");

            }
            textView.setText(stringBuilder.toString());
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    private void showImagePIcDialouge() {
        String options[]={"Camera","Galary"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        dispatchTakePictureIntent();;
                    }

                }else if(which==1) {
                    if(!checkStroagePermission()) {
                        requestStroagePermission();
                    }else {
                        Intent intent=new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,1);

                    }


                }


            }
        });
        builder.create().show();

    }
    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this,camerapermission,CAMERA_REQUEST_CODE);
    }
    private  boolean checkCameraPermission()
    {
        boolean result1= ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        boolean result= ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }
    private  boolean checkStroagePermission()
    {
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void requestStroagePermission()
    {
        ActivityCompat.requestPermissions(this,stroagepermisson,STROAGE_REQUEST_CODE);

    }

}