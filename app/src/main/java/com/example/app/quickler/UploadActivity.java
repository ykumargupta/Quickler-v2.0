package com.example.app.quickler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static android.media.MediaRecorder.VideoSource.CAMERA;

/**
 * Created by Yash Kumar Gupta on 4/10/2018.
 */

public class UploadActivity extends AppCompatActivity {

    String Database_Path = "All_Text_Uploads_Database";

    private static final int GALLERY = 2;
    private Button mSelectBtn ;
    private Button mUploadBtn ;
    private EditText mCaption;

    private final static int FILE_SELECT_CODE = 1;

    private StorageReference mStorageRef;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.user:
                        Intent intent1 = new Intent(UploadActivity.this, MainActivity.class);
                        startActivity(intent1);
                        break;

                    case R.id.bookmark:
                        Intent intent3 = new Intent(UploadActivity.this, BookmarkActivity.class);
                        startActivity(intent3);
                        break;

                    case R.id.upload:
                        break;
                }
                return false;
            }
        });

        mSelectBtn = findViewById(R.id.button2);
        mUploadBtn = findViewById(R.id.button3);
        mCaption = findViewById(R.id.textView7);
        mSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //showPictureDialog();
                openFileSelector();
            }
        });
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTextUploader();
            }
        });



        mStorageRef = FirebaseStorage.getInstance().getReference();
    }
    private void openTextUploader(){
        String textToUpload = mCaption.getText().toString();

        try {
            // Getting image upload ID.
            String TextUploadId = databaseReference.push().getKey();

            // Adding image upload id s child element into databaseReference.
            databaseReference.child(TextUploadId).setValue(textToUpload);

            Toast.makeText(UploadActivity.this,"Text Succesfully Uploaded ", Toast.LENGTH_LONG).show();

        } catch (Exception e){
            Toast.makeText(UploadActivity.this,"Text Upload Failed ", Toast.LENGTH_LONG).show();
        }
    }
/*
    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }
    
    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }*/
    
    private void openFileSelector(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            startActivityForResult(
                    Intent.createChooser(intent,"Select a file to upload "),
                    FILE_SELECT_CODE);

        } catch ( android.content.ActivityNotFoundException ex ){
            Toast.makeText(this,"Please Install a file Manager. ",Toast.LENGTH_SHORT).show();

        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == FILE_SELECT_CODE && resultCode== RESULT_OK){

            Uri fileUri = data.getData();

            String urlString = fileUri.toString();

            File myFile = new File(urlString);
            String path = myFile.getAbsolutePath();

            String displayName = null;

            if(urlString.startsWith("content://")){
                Cursor cursor = null;
                try{
                    cursor = UploadActivity.this.getContentResolver().query(fileUri,null, null,null );
                    if ( cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }


                }finally {
                    cursor.close();

                }
            } else if(urlString.startsWith("file://")) {

                displayName = myFile.getName();


            }





            StorageReference riversRef = mStorageRef.child("files/"+displayName);

            riversRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Toast.makeText(UploadActivity.this,"File Uploaded !. ",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Toast.makeText(UploadActivity.this,"There was an error in uploading. ",Toast.LENGTH_SHORT).show();
                        }
                    });
        }




        super.onActivityResult(requestCode, resultCode, data);



    }
}
