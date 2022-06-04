package com.example.editor;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    HorizontalScrollView toolsLayout;
    ConstraintLayout brightnessLayout, contrastLayout, filtersLayout, framesLayout;
    TextView brightnessBtn, brightnessSeekBarOkView, contrastBtn, contrastSeekBarOkView, cropBtn;
    SeekBar brightnessSeekerBar, contrastSeekerBar;
    BitmapDrawable ogBmp;
    TextView filterBtn, filterBackBtn, frameBtn, frameBackBtn;
    String filtered;
    Bitmap filterBmp, editedBmp, brightBmp;

    ActivityResultLauncher<String> storagePermissionLauncher;
    final String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    TextView openPhotoStoreBtn;
    TextView savePhotoBtn;

    String frameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.photoView);
        ogBmp = (BitmapDrawable) imageView.getDrawable();

        initViews();

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
           if (result){
               getPhotos();
           } else {
               respondToUserOnPermissionActions();
           }
        });

        photoStoreBtns();
    }

    private void initViews() {
        toolsLayout = findViewById(R.id.toolsLayout);

        frameBtn = findViewById(R.id.frameBtn);
        frameBackBtn = findViewById(R.id.frameBackBtn);
        framesLayout = findViewById(R.id.frameBtnsLayout);

        filterBtn = findViewById(R.id.filterBtn);
        filtersLayout = findViewById(R.id.filterBtnsLayout);
        filterBackBtn = findViewById(R.id.filterBackBtn);

        brightnessBtn = findViewById(R.id.brightnessBtn);
        brightnessLayout = findViewById(R.id.brightnessSeekBarLayout);
        brightnessSeekBarOkView = findViewById(R.id.brightnessSeekBarOkView);
        brightnessSeekerBar = findViewById(R.id.brightnessSeekBar);

        contrastBtn = findViewById(R.id.contrastBtn);
        contrastLayout = findViewById(R.id.contrastSeekBarLayout);
        contrastSeekBarOkView = findViewById(R.id.contrastSeekBarOkView);
        contrastSeekerBar = findViewById(R.id.contrastSeekBar);

        cropBtn = findViewById(R.id.cropBtn);

        frameBtn.setOnClickListener(view -> {
            toolsLayout.setVisibility(View.GONE);
            framesLayout.setVisibility(View.VISIBLE);
        });
        frameBackBtn.setOnClickListener(view -> {
            framesLayout.setVisibility(View.GONE);
            toolsLayout.setVisibility(View.VISIBLE);
        });

        filterBtn.setOnClickListener(view -> {
            toolsLayout.setVisibility(View.GONE);
            filtersLayout.setVisibility(View.VISIBLE);
        });
        filterBackBtn.setOnClickListener(view -> {
            filtersLayout.setVisibility(View.GONE);
            toolsLayout.setVisibility(View.VISIBLE);
        });

        brightnessBtn.setOnClickListener(view -> {
            toolsLayout.setVisibility(View.GONE);
            brightnessLayout.setVisibility(View.VISIBLE);
        });
        brightnessSeekBarOkView.setOnClickListener(view -> {
            brightnessLayout.setVisibility(View.GONE);
            toolsLayout.setVisibility(View.VISIBLE);

        });

        contrastBtn.setOnClickListener(view -> {
            toolsLayout.setVisibility(View.GONE);
            contrastLayout.setVisibility(View.VISIBLE);
        });
        contrastSeekBarOkView.setOnClickListener(view -> {
            contrastLayout.setVisibility(View.GONE);
            toolsLayout.setVisibility(View.VISIBLE);

        });

        cropBtn.setOnClickListener(view -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap inImage = drawable.getBitmap();

            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), inImage, "Title", null);
            Uri uri = Uri.parse(path);

            CropImage.activity(uri).start(this);
        });

        frames();
        filters();
        seekBarListerner();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    imageView.setImageBitmap(bitmap);
                    ogBmp = (BitmapDrawable) imageView.getDrawable();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void photoStoreBtns() {
        openPhotoStoreBtn = findViewById(R.id.addBtn);
        openPhotoStoreBtn.setOnClickListener(view -> storagePermissionLauncher.launch(storagePermission));

        savePhotoBtn = findViewById(R.id.saveBtn);
        savePhotoBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(this).setTitle("Are you sure")
                    .setMessage("Save it")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editedBmp != null) {
                                savePhoto(editedBmp);
                            } else if (brightBmp != null){
                                savePhoto(brightBmp);
                            } else if (filterBmp != null){
                                savePhoto(filterBmp);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        });
    }

    private void savePhoto(Bitmap bitmap){
        if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED){
            ContentResolver  contentResolver = getContentResolver();
            if (isExternalStorageWritable()){
                Uri photoCollectionUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    photoCollectionUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    photoCollectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String photoName = getResources().getString((R.string.app_name))+"_"+timeStamp+".jpg";
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, photoName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/"+getResources().getString(R.string.app_name));
                }

                Uri photoUri = contentResolver.insert(photoCollectionUri, contentValues);
                try {
                    OutputStream fos = contentResolver.openOutputStream(photoUri);
                    boolean isSaved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    if (isSaved){
                        Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
                    }
                    fos.flush();
                    fos.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } else {
            storagePermissionLauncher.launch(storagePermission);
        }

    }

    private boolean isExternalStorageWritable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private void getPhotos(){
        List<Photo> photos = new ArrayList<>();
        Uri libraryUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            libraryUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            libraryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try(Cursor cursor = getContentResolver().query(libraryUri, projection, null, null, sortOrder)) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            int bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            int bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()){
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String date = cursor.getString(dateColumn);
                int size = cursor.getInt(sizeColumn);
                long bucketId = cursor.getLong(bucketIdColumn);
                String bucketName = cursor.getString(bucketNameColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                name = name.substring(0, name.lastIndexOf("."));
                Photo photo = new Photo(id, String.valueOf(uri), date, size, bucketId, bucketName);

                photos.add(photo);
            }
        }

        showPhotos(photos);
    }

    private void showPhotos(List<Photo> photos){
        if (photos.size() > 0){
            PhotoBottomSheetFragment bottomSheetFragment = new PhotoBottomSheetFragment(photos);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            Toast.makeText(getApplicationContext(), "No photos available, Add some", Toast.LENGTH_SHORT).show();
        }
    }

    private void respondToUserOnPermissionActions() {
        if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED){
            getPhotos();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (shouldShowRequestPermissionRationale(storagePermission)){
                new AlertDialog.Builder(this)
                        .setTitle("Request Permission")
                        .setMessage("Allow us to show and save photos")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                storagePermissionLauncher.launch(storagePermission);
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "Not Permission", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
            else {
                Toast.makeText(getApplicationContext(), "not shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seekBarListerner() {
        brightnessSeekerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                adjustBrightness(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        contrastSeekerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                adjustContrast(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void adjustBrightness(int value) {
        Bitmap bmp = ogBmp.getBitmap();
        if (filterBmp != null){
            bmp = filterBmp;
        }

        final int mul = 0XFFFFFF;

        String initialHex = Tool.hexScale()[value];
        String initialAdd = "0X"+initialHex + initialHex + initialHex;
        int add = Integer.decode(initialAdd);

        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        ColorFilter colorFilter = new LightingColorFilter(mul, add);
        paint.setColorFilter(colorFilter);

        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawBitmap(outputBitmap, 0, 0, paint);

//        imageView.setImageBitmap(outputBitmap);
        brightBmp = outputBitmap;
        if (frameType != null){
            frame(frameType, brightBmp);
        } else {
            imageView.setImageBitmap(outputBitmap);
        }

    }

    private void adjustContrast(int value) {
        Bitmap bmp = ogBmp.getBitmap();
        if (filterBmp != null){
            bmp = filterBmp;
        }

        String initialHex = Tool.hexScale()[value];
        String initialMul = "0X"+initialHex + initialHex + initialHex;
        int mul = Integer.decode(initialMul);
        int add = 0X000000;

        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        ColorFilter colorFilter = new LightingColorFilter(mul, add);
        paint.setColorFilter(colorFilter);

        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawBitmap(outputBitmap, 0, 0, paint);

//        imageView.setImageBitmap(outputBitmap);
        brightBmp = outputBitmap;
        if (frameType != null){
            frame(frameType, brightBmp);
        } else {
            imageView.setImageBitmap(outputBitmap);
        }
    }

    private void frames() {
        ImageView frame1Btn = findViewById(R.id.frame1Btn);
        frame1Btn.setOnClickListener(view -> frame(Frame.frame1));
        frameBtn(frame1Btn, Frame.frame1);

        ImageView reFreshBtn = findViewById(R.id.refreshFrameBtn);
        reFreshBtn.setOnClickListener(view -> resetFrame());
    }

    private void resetFrame(){
        frameType = null;
        editedBmp = null;
        frame(Frame.frame0);
    }

    private void frame(String frame) {
        Bitmap bmp = ogBmp.getBitmap();
        if (brightBmp != null){
            bmp = brightBmp;
        } else if (filterBmp != null) {
            bmp = filterBmp;
        }

        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);

        if (frame.equalsIgnoreCase(Frame.frame1)){
            int offset = 10;
            int firstFrameWidth = offset;
            int secondFrameWidth = offset/2;
            int thirdFrameWidth = offset/3;
            int fourthFramewidth = (offset+2) - (offset/2);
            if (bmp.getWidth()>=1000 && bmp.getHeight() >= 1000){
                offset = offset+4;
                firstFrameWidth = offset;
                secondFrameWidth = offset/2;
                thirdFrameWidth = offset/3;
                fourthFramewidth = (offset+2) - (offset/2);
            }

            canvas.drawColor(Color.parseColor("#523327"));
            canvas.drawRect(strokeRect(firstFrameWidth+secondFrameWidth+thirdFrameWidth,
                    firstFrameWidth+secondFrameWidth+thirdFrameWidth,
                    bmp.getWidth()-(firstFrameWidth+secondFrameWidth+thirdFrameWidth),
                    bmp.getHeight()-(firstFrameWidth+secondFrameWidth+thirdFrameWidth)),
                    strokePaint(Color.parseColor("#e4d3a0"), fourthFramewidth));
            Rect src_dst = strokeRect(firstFrameWidth+secondFrameWidth+fourthFramewidth,
                    firstFrameWidth+secondFrameWidth+fourthFramewidth,
                    bmp.getWidth()-(firstFrameWidth+secondFrameWidth+fourthFramewidth),
                    bmp.getHeight()-(firstFrameWidth+secondFrameWidth+fourthFramewidth));
            canvas.drawBitmap(bmp, src_dst, src_dst, null);
        }
        imageView.setImageBitmap(outputBitmap);
        editedBmp = outputBitmap;
        frameType = frame;
    }

    private void frame(String frame, Bitmap bitmap) {
        Bitmap bmp = ogBmp.getBitmap();
        bmp = bitmap;

        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);

        if (frame.equalsIgnoreCase(Frame.frame1)){
            int offset = 10;
            int firstFrameWidth = offset;
            int secondFrameWidth = offset/2;
            int thirdFrameWidth = offset/3;
            int fourthFramewidth = (offset+2) - (offset/2);
            if (bmp.getWidth()>=1000 && bmp.getHeight() >= 1000){
                offset = offset+4;
                firstFrameWidth = offset;
                secondFrameWidth = offset/2;
                thirdFrameWidth = offset/3;
                fourthFramewidth = (offset+2) - (offset/2);
            }

            canvas.drawColor(Color.parseColor("#523327"));
            canvas.drawRect(strokeRect(firstFrameWidth+secondFrameWidth+thirdFrameWidth,
                    firstFrameWidth+secondFrameWidth+thirdFrameWidth,
                    bmp.getWidth()-(firstFrameWidth+secondFrameWidth+thirdFrameWidth),
                    bmp.getHeight()-(firstFrameWidth+secondFrameWidth+thirdFrameWidth)),
                    strokePaint(Color.parseColor("#e4d3a0"), fourthFramewidth));
            Rect src_dst = strokeRect(firstFrameWidth+secondFrameWidth+fourthFramewidth,
                    firstFrameWidth+secondFrameWidth+fourthFramewidth,
                    bmp.getWidth()-(firstFrameWidth+secondFrameWidth+fourthFramewidth),
                    bmp.getHeight()-(firstFrameWidth+secondFrameWidth+fourthFramewidth));
            canvas.drawBitmap(bmp, src_dst, src_dst, null);
        }
        imageView.setImageBitmap(outputBitmap);
        editedBmp = outputBitmap;
        frameType = frame;
    }

    private void frameBtn(ImageView btn, String frame) {
        Bitmap bmp = ogBmp.getBitmap();
        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);

        if (frame.equalsIgnoreCase(Frame.frame1)){
            int offset = 25;
            int firstFrameWidth = offset;
            int secondFrameWidth = offset/2;
            int thirdFrameWidth = offset/3;
            int fourthFramewidth = (offset+2) - (offset/2);
            if (bmp.getWidth()>=1000 && bmp.getHeight() >= 1000){
                offset = offset+4;
                firstFrameWidth = offset;
                secondFrameWidth = offset/2;
                thirdFrameWidth = offset/3;
                fourthFramewidth = (offset+2) - (offset/2);
            }

            canvas.drawColor(Color.parseColor("#523327"));
            canvas.drawRect(strokeRect(firstFrameWidth+secondFrameWidth+thirdFrameWidth,
                    firstFrameWidth+secondFrameWidth+thirdFrameWidth+125,
                    bmp.getWidth()-(firstFrameWidth+secondFrameWidth+thirdFrameWidth),
                    bmp.getHeight()-(firstFrameWidth+secondFrameWidth+thirdFrameWidth)),
                    strokePaint(Color.parseColor("#e4d3a0"), fourthFramewidth));
            Rect src_dst = strokeRect(firstFrameWidth+secondFrameWidth+fourthFramewidth,
                    firstFrameWidth+secondFrameWidth+fourthFramewidth+125,
                    bmp.getWidth()-(firstFrameWidth+secondFrameWidth+fourthFramewidth),
                    bmp.getHeight()-(firstFrameWidth+secondFrameWidth+fourthFramewidth));
            canvas.drawBitmap(bmp, src_dst, src_dst, null);
        }
        btn.setImageBitmap(outputBitmap);
    }

    Rect strokeRect(int left, int top, int right, int bottom) {
        return new Rect(left, top, right, bottom);
    }

    Paint strokePaint(int color, int width){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        return paint;
    }

    private void filters() {
//        Grey filter Btn
        ImageView greyBtn = findViewById(R.id.greyBtn);
        filterBtn(greyBtn, Filter.grey);
        greyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.grey);
            }
        });
//        Red filter Btn
        ImageView redBtn = findViewById(R.id.redBtn);
        filterBtn(redBtn, Filter.red);
        redBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.red);
            }
        });
//        Green filter Btn
        ImageView greenBtn = findViewById(R.id.greenBtn);
        filterBtn(greenBtn, Filter.green);
        greenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.green);
            }
        });
//        Blue filter Btn
        ImageView blueBtn = findViewById(R.id.blueBtn);
        filterBtn(blueBtn, Filter.blue);
        blueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.blue);
            }
        });
//        RedGreen filter Btn
        ImageView redGreenBtn = findViewById(R.id.redGreenBtn);
        filterBtn(redGreenBtn, Filter.redGreen);
        redGreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.redGreen);
            }
        });
//        RedBlue filter Btn
        ImageView redBlueBtn = findViewById(R.id.redBlueBtn);
        filterBtn(redBlueBtn, Filter.redBlue);
        redBlueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.redBlue);
            }
        });
//        GreenBlue filter Btn
        ImageView greenBlueBtn = findViewById(R.id.greenBlueBtn);
        filterBtn(greenBlueBtn, Filter.greenBlue);
        greenBlueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.greenBlue);
            }
        });
//        Sepia filter Btn
        ImageView sepiaBtn = findViewById(R.id.sepiaBtn);
        filterBtn(sepiaBtn, Filter.sepia);
        sepiaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.sepia);
            }
        });
//        Binary filter Btn
        ImageView binaryBtn = findViewById(R.id.binaryBtn);
        filterBtn(binaryBtn, Filter.binary);
        binaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.binary);
            }
        });
//        Invert filter Btn
        ImageView invertBtn = findViewById(R.id.invertBtn);
        filterBtn(invertBtn, Filter.invert);
        invertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(Filter.invert);
            }
        });

        ImageView ogPhotoBtn = findViewById(R.id.filterOgBtn);
        ogPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFilter();
            }
        });
    }

    private void resetFilter(){
        filterBmp = null;
        filtered = null;
        if (frameType != null) {
            frame(frameType);
        } else {
            imageView.setImageDrawable(ogBmp);
        }

        brightnessSeekerBar.setProgress(0);
        contrastSeekerBar.setProgress(255);
    }

    private void filter(String filter){
        Bitmap bmp = ogBmp.getBitmap();

        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(outputBitmap);

//        Grey filter
        if (filter.equalsIgnoreCase(Filter.grey)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }

//        Red filter
        if (filter.equalsIgnoreCase(Filter.red)){
            final int mul = 0XFF0000;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Green filter
        if (filter.equalsIgnoreCase(Filter.green)){
            final int mul = 0X00FF00;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Blue filter
        if (filter.equalsIgnoreCase(Filter.blue)){
            final int mul = 0X0000FF;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Red Green filter
        if (filter.equalsIgnoreCase(Filter.redGreen)){
            final int mul = 0XFFFF00;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Red Blue filter
        if (filter.equalsIgnoreCase(Filter.redBlue)){
            final int mul = 0XFF00FF;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Green Blue filter
        if (filter.equalsIgnoreCase(Filter.greenBlue)){
            final int mul = 0X00FFFF;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
        imageView.setImageBitmap(outputBitmap);
//        Sepia filter
        if (filter.equalsIgnoreCase(Filter.sepia)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);

            ColorMatrix colorScale = new ColorMatrix();
            colorScale.setScale(1,1,0.8f,1);

            colorMatrix.postConcat(colorScale);

            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }

//        Binary filter
        if (filter.equalsIgnoreCase(Filter.binary)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);

            float m = 255f;
            float t = -255*128f;
            ColorMatrix threshold = new ColorMatrix(new float[]{
                    m, 0, 0, 1, t,
                    0, m, 0, 1, t,
                    0, 0, m, 1, t,
                    0, 0, 0, 1, 0
            });

            colorMatrix.postConcat(threshold);

            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Invert filter
        if (filter.equalsIgnoreCase(Filter.invert)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            colorMatrix.set(new float[]{
                    -1, 0, 0, 0, 255,
                    0, -1, 0, 0, 255,
                    0, 0, -1, 0, 255,
                    0, 0, 0, 1, 0
            });

            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }

//        imageView.setImageBitmap(outputBitmap);
        filterBmp = outputBitmap;
        if (frameType != null) {
            frame(frameType, filterBmp);
        } else {
            imageView.setImageBitmap(outputBitmap);
        }
        filtered = filter;
    }

    private void filterBtn(ImageView btn, String filter){
        BitmapDrawable dBmp = (BitmapDrawable) btn.getDrawable();

        Bitmap bmp = dBmp.getBitmap();
        Bitmap outputBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(outputBitmap);

//        Grey filter
        if (filter.equalsIgnoreCase(Filter.grey)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Red filter
        if (filter.equalsIgnoreCase(Filter.red)){
            final int mul = 0XFF0000;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Green filter
        if (filter.equalsIgnoreCase(Filter.green)){
            final int mul = 0X00FF00;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Blue filter
        if (filter.equalsIgnoreCase(Filter.blue)){
            final int mul = 0X0000FF;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Red Green filter
        if (filter.equalsIgnoreCase(Filter.redGreen)){
            final int mul = 0XFFFF00;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Red Blue filter
        if (filter.equalsIgnoreCase(Filter.redBlue)){
            final int mul = 0XFF00FF;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Green Blue filter
        if (filter.equalsIgnoreCase(Filter.greenBlue)){
            final int mul = 0X00FFFF;
            final int add = 0X000000;
            ColorFilter colorFilter = new LightingColorFilter(mul, add);
            paint.setColorFilter(colorFilter);
            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Sepia filter
        if (filter.equalsIgnoreCase(Filter.sepia)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);

            ColorMatrix colorScale = new ColorMatrix();
            colorScale.setScale(1,1,0.8f,1);

            colorMatrix.postConcat(colorScale);

            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }

//        Binary filter
        if (filter.equalsIgnoreCase(Filter.binary)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);

            float m = 255f;
            float t = -255*128f;
            ColorMatrix threshold = new ColorMatrix(new float[]{
                    m, 0, 0, 1, t,
                    0, m, 0, 1, t,
                    0, 0, m, 1, t,
                    0, 0, 0, 1, 0
            });

            colorMatrix.postConcat(threshold);

            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
//        Invert filter
        if (filter.equalsIgnoreCase(Filter.invert)){
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            colorMatrix.set(new float[]{
                    -1, 0, 0, 0, 255,
                    0, -1, 0, 0, 255,
                    0, 0, -1, 0, 255,
                    0, 0, 0, 1, 0
            });

            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorFilter);

            canvas.drawBitmap(outputBitmap, 0, 0, paint);
        }
        btn.setImageBitmap(outputBitmap);
    }


}