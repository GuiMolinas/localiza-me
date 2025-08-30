package com.queridinhos.tcc;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ScheduleActivity extends AppCompatActivity {

    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 100;
    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private FloatingActionButton addEventFab;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        calendarView = findViewById(R.id.calendarView);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        addEventFab = findViewById(R.id.addEventFab);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        selectedDate = System.currentTimeMillis();

        setupRecyclerView();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
            checkCalendarPermissions();
        });

        addEventFab.setOnClickListener(v -> showAddOrUpdateEventDialog(null));

        checkCalendarPermissions();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(eventList, this::showAddOrUpdateEventDialog);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    private void checkCalendarPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, CALENDAR_PERMISSION_REQUEST_CODE);
        } else {
            loadEventsForDate(selectedDate);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadEventsForDate(selectedDate);
            } else {
                Toast.makeText(this, "Permissão de calendário negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadEventsForDate(long date) {
        eventList.clear();
        ContentResolver contentResolver = getContentResolver();

        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTimeInMillis(date);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTimeInMillis(date);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startOfDay.getTimeInMillis());
        ContentUris.appendId(builder, endOfDay.getTimeInMillis());

        String[] projection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END
        };

        Cursor cursor = contentResolver.query(builder.build(), projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                long startDate = cursor.getLong(3);
                long endDate = cursor.getLong(4);

                boolean isRecurring = false;
                Cursor eventCursor = getContentResolver().query(
                        CalendarContract.Events.CONTENT_URI,
                        new String[]{CalendarContract.Events.RRULE},
                        CalendarContract.Events._ID + " = ?",
                        new String[]{String.valueOf(eventId)},
                        null
                );
                if (eventCursor != null && eventCursor.moveToFirst()) {
                    String rrule = eventCursor.getString(0);
                    isRecurring = rrule != null && !rrule.isEmpty();
                    eventCursor.close();
                }
                eventList.add(new Event(eventId, title, description, startDate, endDate, isRecurring));
            }
            cursor.close();
        }
        eventAdapter.setEvents(eventList);
    }

    private void showAddOrUpdateEventDialog(final Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        final EditText titleInput = view.findViewById(R.id.eventTitleInput);
        final EditText descriptionInput = view.findViewById(R.id.eventDescriptionInput);
        final CheckBox recurringCheckbox = view.findViewById(R.id.recurringEventCheckbox);
        final EditText startTimeInput = view.findViewById(R.id.startTime);
        final EditText endTimeInput = view.findViewById(R.id.endTime);

        Calendar calendar = Calendar.getInstance();

        startTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> startTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        endTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view12, hourOfDay, minute) -> endTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });


        if (event != null) {
            builder.setTitle("Atualizar Evento");
            titleInput.setText(event.getTitle());
            descriptionInput.setText(event.getDescription());
            recurringCheckbox.setChecked(event.isRecurring());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            startTimeInput.setText(sdf.format(new Date(event.getStartDate())));
            endTimeInput.setText(sdf.format(new Date(event.getEndDate())));

        } else {
            builder.setTitle("Adicionar Evento");
        }

        builder.setView(view);

        builder.setPositiveButton(event != null ? "Atualizar" : "Adicionar", (dialog, which) -> {
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            boolean isRecurring = recurringCheckbox.isChecked();

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(selectedDate);
            String[] startTime = startTimeInput.getText().toString().split(":");
            if (startTime.length == 2) {
                startCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
                startCalendar.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
            }


            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(selectedDate);
            String[] endTime = endTimeInput.getText().toString().split(":");
            if(endTime.length == 2) {
                endCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
                endCalendar.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
            }

            if (event != null) {
                updateEvent(event.getId(), title, description, isRecurring, startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis());
            } else {
                addEvent(title, description, startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis(), isRecurring);
            }
        });

        if (event != null) {
            builder.setNeutralButton("Excluir", (dialog, which) -> deleteEvent(event.getId()));
        }

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addEvent(String title, String description, long startTime, long endTime, boolean isRecurring) {
        long calID = 1;

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        if (isRecurring) {
            values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            checkCalendarPermissions();
            return;
        }
        cr.insert(CalendarContract.Events.CONTENT_URI, values);

        Toast.makeText(this, "Evento adicionado", Toast.LENGTH_SHORT).show();
        loadEventsForDate(selectedDate);
    }

    private void updateEvent(long eventId, String title, String description, boolean isRecurring, long startTime, long endTime) {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);

        if (isRecurring) {
            values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY");
        } else {
            values.putNull(CalendarContract.Events.RRULE);
        }

        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        cr.update(updateUri, values, null, null);

        Toast.makeText(this, "Evento atualizado", Toast.LENGTH_SHORT).show();
        loadEventsForDate(selectedDate);
    }

    private void deleteEvent(long eventId) {
        ContentResolver cr = getContentResolver();
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        cr.delete(deleteUri, null, null);

        Toast.makeText(this, "Evento excluído", Toast.LENGTH_SHORT).show();
        loadEventsForDate(selectedDate);
    }
}