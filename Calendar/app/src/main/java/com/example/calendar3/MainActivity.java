package com.example.calendar3;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button Important_calendar;
    private String TAG = "test";
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Important_calendar = (Button) findViewById(R.id.Import_Image);

        Important_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will read text from document and store into Google Calendar
                onImageGalleryClicked(v);

            }
        });
    }

    private void onImageGalleryClicked(View view) {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    @TargetApi(24)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                List<String[]> events = new ArrayList<String[]>();
                String regex = "\\d+/\\d+";
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                Log.i("detected!", String.valueOf(textBlocks.size()));

                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.valueAt(i);
                    String text = textBlock.getValue();
                    Log.i(String.valueOf(i), text);
                    //for (int j = 0; j < text.length; j++) {
                    if (text.contains("02/20")) {
                        events.add(text.split("\\s+"));
                    }
                    //}
                }
                int countEvent = events.size();
                Log.i("count", String.valueOf(countEvent));
                for (int i = 0; i < countEvent; i++) {
                    Log.i("addEvent", events.get(i)[1]);
                    long calID = i;
                    long startMillis = 0;
                    long endMillis = 0;
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.set(2017, 2, 20, 7, 30);
                    startMillis = beginTime.getTimeInMillis();
                    Calendar endTime = Calendar.getInstance();
                    endTime.set(2017, 2, 20, 8, 45);
                    endMillis = endTime.getTimeInMillis();
...

                    ContentResolver cr = getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DTSTART, startMillis);
                    values.put(CalendarContract.Events.DTEND, endMillis);
                    values.put(CalendarContract.Events.TITLE, "Topic");
                    values.put(CalendarContract.Events.DESCRIPTION, Arrays.toString(
                            Arrays.copyOfRange(events.get(i), 2, events.get(i).length)));
                    values.put(CalendarContract.Events.CALENDAR_ID, calID);
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Uri carUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

// get the event ID that is the last element in the Uri
                    long eventID = Long.parseLong(carUri.getLastPathSegment());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
