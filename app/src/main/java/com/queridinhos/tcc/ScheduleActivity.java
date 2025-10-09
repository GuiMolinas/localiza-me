package com.queridinhos.tcc;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import java.util.concurrent.atomic.AtomicInteger;

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

        NotificationHelper.createNotificationChannel(this);

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
            checkPermissions();
        });

        addEventFab.setOnClickListener(v -> showAddOrUpdateEventDialog(null));

        checkPermissions();
        setupTestNotificationButton();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(eventList, this::showAddOrUpdateEventDialog);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    private void checkPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CALENDAR);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_CALENDAR);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), CALENDAR_PERMISSION_REQUEST_CODE);
        } else {
            loadEventsForDate(selectedDate);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                loadEventsForDate(selectedDate);
            } else {
                Toast.makeText(this, "Permissões necessárias negadas.", Toast.LENGTH_SHORT).show();
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }

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
            checkPermissions();
            return;
        }
        Uri newEvent = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        if (newEvent != null) {
            long eventId = Long.parseLong(newEvent.getLastPathSegment());
            scheduleNotifications(eventId, title, description, startTime);
            Toast.makeText(this, "Evento adicionado", Toast.LENGTH_SHORT).show();
            loadEventsForDate(selectedDate);
        }
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

        // Sempre cancele as notificações antigas
        cancelNotifications(eventId);
        // E reagende as novas (o método scheduleNotifications já verifica o switch)
        scheduleNotifications(eventId, title, description, startTime);

        Toast.makeText(this, "Evento atualizado", Toast.LENGTH_SHORT).show();
        loadEventsForDate(selectedDate);
    }

    private void deleteEvent(long eventId) {
        ContentResolver cr = getContentResolver();
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        cr.delete(deleteUri, null, null);

        cancelNotifications(eventId);

        Toast.makeText(this, "Evento excluído", Toast.LENGTH_SHORT).show();
        loadEventsForDate(selectedDate);
    }

    private void scheduleNotifications(long eventId, String title, String description, long startTime) {
        SharedPreferences prefs = getSharedPreferences("NotificationsPrefs", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", true);

        if (!notificationsEnabled) {
            return; // Se as notificações estiverem desativadas, não faz nada
        }

        long[] notificationTimes = {
                startTime - 60 * 60 * 1000, // 1 hora antes
                startTime - 30 * 60 * 1000, // 30 mins antes
                startTime - 10 * 60 * 1000  // 10 mins antes
        };

        for (int i = 0; i < notificationTimes.length; i++) {
            if (notificationTimes[i] > System.currentTimeMillis()) {
                scheduleSingleNotification(eventId, title, description, notificationTimes[i], i);
            }
        }
    }

    private void scheduleSingleNotification(long eventId, String title, String description, long time, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("notification_id", (int) eventId + requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) eventId + requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        }
    }


    private void cancelNotifications(long eventId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 3; i++) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, (int) eventId + i, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private void setupTestNotificationButton() {
        View testButton = findViewById(R.id.testNotificationButton);
        final AtomicInteger clickCount = new AtomicInteger(0);

        testButton.setOnClickListener(v -> {
            int count = clickCount.incrementAndGet();
            if (count >= 5) {
                SharedPreferences prefs = getSharedPreferences("NotificationsPrefs", MODE_PRIVATE);
                boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", true);

                if (notificationsEnabled) {
                    Toast.makeText(this, "Notificação de teste em 5 segundos!", Toast.LENGTH_SHORT).show();
                    scheduleSingleNotification(
                            -1,
                            "Notificação de Teste",
                            "Se você está vendo isso, está funcionando!",
                            System.currentTimeMillis() + 5000,
                            999
                    );
                } else {
                    Toast.makeText(this, "As notificações estão desativadas!", Toast.LENGTH_SHORT).show();
                }
                clickCount.set(0); // Reseta a contagem
            }
        });
    }
}