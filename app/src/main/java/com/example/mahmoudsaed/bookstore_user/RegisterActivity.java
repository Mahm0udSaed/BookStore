package com.example.mahmoudsaed.bookstore_user;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivProfile;
    private EditText etEmail, etPassword, etName, etAddress;
    private Button btnRegister;
    private ProgressBar progressBar;
    private String mCurrentPhotoPath;
    private File imageFile;
    private Uri selectedImageUri;
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String email, name, address;
    private Publisher publisher;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //Todo Save Author Info & Profile Image
                    publisher = new Publisher(email, user.getUid());
                    publisher.setAddress(address);
                    publisher.setName(name);
                    if (imageFile != null)
                        uploadUserProfileImage(user.getUid());
                    else
                        addNewPublisher();
                }
            }
        };
    }

    private void uploadUserProfileImage(String id) {

        //Todo Compress Image File

        Uri photoURI = FileProvider.getUriForFile(RegisterActivity.this, getString(R.string.provider), imageFile);

        StorageReference profileImagesRef = mStorageRef.child(Constants.PROFILE_IMAGES_FOLDER + id + ".jpg");

        profileImagesRef.putFile(photoURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String imageUrl = taskSnapshot.getDownloadUrl().toString();
                        publisher.setImageUrl(imageUrl);
                        addNewPublisher();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        publisher.setImageUrl("");
                        addNewPublisher();
                    }
                });
    }

    private void addNewPublisher() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Constants.REF_PUBLISHER);
        myRef.child(publisher.getId()).setValue(publisher).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
                ActivityLauncher.openMyBooksActivity(RegisterActivity.this);
                finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void initViews() {
        btnRegister = (Button) findViewById(R.id.btn_register);
        ivProfile = (ImageView) findViewById(R.id.iv_profile);
        etEmail = (EditText) findViewById(R.id.et_email);
        etName = (EditText) findViewById(R.id.et_name);
        etPassword = (EditText) findViewById(R.id.et_password);
        etAddress = (EditText) findViewById(R.id.et_address);

        progressBar = (ProgressBar) findViewById(R.id.progress_register);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);


        ivProfile.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }


    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose)
                .setCancelable(true)
                .setItems(new String[]{getString(R.string.gallery), getString(R.string.camera)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent i = new Intent(Intent.ACTION_PICK);
                            i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, GALLERY_REQUEST);
                        } else {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(RegisterActivity.this, getString(R.string.provider), photoFile);
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                                }
                            }
                        }
                    }
                }).show();
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivProfile.getWidth();
        int targetH = ivProfile.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if (targetH != 0 && targetW != 0
                && photoW != 0 && photoH != 0) {
            // Determine how much to scale down the image
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ivProfile.setImageBitmap(bitmap);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.path), mCurrentPhotoPath);
        outState.putSerializable(getString(R.string.file), imageFile);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString(getString(R.string.path));
        imageFile = (File) savedInstanceState.getSerializable(getString(R.string.file));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.date), Locale.ENGLISH).format(new Date());
        String imageFileName = getString(R.string.jpeg) + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                getString(R.string.jpg),         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                readFileFromSelectedURI();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            setPic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readFileFromSelectedURI();
        } else {
            Toast.makeText(this, R.string.cannot_read_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void readFileFromSelectedURI() {
        Cursor cursor = getContentResolver().query(selectedImageUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(0);
            cursor.close();
            imageFile = new File(imagePath);
            Bitmap image = BitmapFactory.decodeFile(imagePath);
            ivProfile.setImageBitmap(image);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_profile)
            showImagePickerDialog();
        else {
            registerNewUser();
        }
    }

    private void registerNewUser() {
        email = etEmail.getText().toString();
        name = etName.getText().toString();
        address = etAddress.getText().toString();
        String password = etPassword.getText().toString();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.enter_name));
        } else if (email.isEmpty()) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.email_not_formatted));
        } else if (password.isEmpty()) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (etPassword.length() < 6) {
            etPassword.setError(getString(R.string.password_length_error));
        } else {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.GONE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setVisibility(View.VISIBLE);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException)
                            Toast.makeText(RegisterActivity.this, R.string.email_already_exist, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(RegisterActivity.this, R.string.error_in_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}
