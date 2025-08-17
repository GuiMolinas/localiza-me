//testando mundança
package com.queridinhos.tcc;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends BaseActivity implements View

        .OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        //Botões
        CardView mapButton = findViewById(R.id.mapButton);
        CardView localizeButton = findViewById(R.id.localizeButton);
        CardView myRoutesButton = findViewById(R.id.myRoutesButton);
        CardView scheduleButton = findViewById(R.id.scheduleButton);
        CardView contactsButton = findViewById(R.id.contactsButton);
        ImageButton exitButton = findViewById(R.id.btnExit);
        ImageButton settingsButton = findViewById(R.id.btnSettings);

        mapButton.setOnClickListener(this);
        localizeButton.setOnClickListener(this);
        myRoutesButton.setOnClickListener(this);
        scheduleButton.setOnClickListener(this);
        contactsButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

    }


    //Clique
    @Override
    public void onClick(View v) {
        Intent intent;
        int id = v.getId();

        if (id == R.id.mapButton) {
            intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.localizeButton) {
            intent = new Intent(this, LocalizeActivity.class);
            startActivity(intent);
        } else if (id == R.id.myRoutesButton) {
            intent = new Intent(this, RoutesActivity.class);
            startActivity(intent);
        } else if (id == R.id.scheduleButton) {
            intent = new Intent(this, ScheduleActivity.class);
            startActivity(intent);
        } else if (id == R.id.contactsButton) {
            intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        } else if (id == R.id.btnExit) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmação")
                    .setMessage("Deseja sair do app?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        } else if (id == R.id.btnSettings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }
}
