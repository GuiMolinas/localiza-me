package com.queridinhos.tcc;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends BaseActivity {

    // PREFERENCES KEYS
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String THEME_KEY = "selectedTheme";
    private static final String FONT_SIZE_PREFS = "FontSizePrefs";
    private static final String FONT_SIZE_KEY = "selectedFontSize";
    private static final String VISUALIZAR_CLIQUE_PREFS = "VisualizarCliquePrefs";
    private static final String VISUALIZAR_CLIQUE_KEY = "visualizarClique";
    private static final String COORDENADAS_PREFS = "CoordenadasPrefs";
    private static final String COORDENADAS_KEY = "visualizarCoordenadas";
    private static final String NOTIFICATIONS_PREFS = "NotificationsPrefs";
    private static final String NOTIFICATIONS_KEY = "notificationsEnabled";

    private ValueAnimator currentAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
    }

    private void setupUI() {
        setupBackButton();
        setupLogoutButton();
        setupAboutAppButton();
        setupThemeSpinner();
        setupFontSizeSpinner();
        setupVisualizarCliqueSwitch();
        setupCoordenadasSwitch();
        setupNotificationsSwitch(); // Adicionado
    }

    private void setupNotificationsSwitch() {
        SwitchCompat notificationsSwitch = findViewById(R.id.notificationsSwitch);
        SharedPreferences prefs = getSharedPreferences(NOTIFICATIONS_PREFS, MODE_PRIVATE);
        // Notificações ativadas por padrão (true)
        notificationsSwitch.setChecked(prefs.getBoolean(NOTIFICATIONS_KEY, true));

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences(NOTIFICATIONS_PREFS, MODE_PRIVATE).edit();
            editor.putBoolean(NOTIFICATIONS_KEY, isChecked);
            editor.apply();
        });
    }

    private void setupVisualizarCliqueSwitch() {
        SwitchCompat visualizarCliqueSwitch = findViewById(R.id.visualizarCliqueSwitch);
        SharedPreferences prefs = getSharedPreferences(VISUALIZAR_CLIQUE_PREFS, MODE_PRIVATE);
        visualizarCliqueSwitch.setChecked(prefs.getBoolean(VISUALIZAR_CLIQUE_KEY, false));

        visualizarCliqueSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences(VISUALIZAR_CLIQUE_PREFS, MODE_PRIVATE).edit();
            editor.putBoolean(VISUALIZAR_CLIQUE_KEY, isChecked);
            editor.apply();
        });
    }

    private void setupCoordenadasSwitch() {
        SwitchCompat coordenadasSwitch = findViewById(R.id.coordenadasSwitch);
        SharedPreferences prefs = getSharedPreferences(COORDENADAS_PREFS, MODE_PRIVATE);
        coordenadasSwitch.setChecked(prefs.getBoolean(COORDENADAS_KEY, false));

        coordenadasSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences(COORDENADAS_PREFS, MODE_PRIVATE).edit();
            editor.putBoolean(COORDENADAS_KEY, isChecked);
            editor.apply();
        });
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finishWithAnimation());
    }

    private void finishWithAnimation() {
        View rootView = findViewById(android.R.id.content);
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        currentAnimator = ValueAnimator.ofFloat(1f, 0.9f);
        currentAnimator.setDuration(150);
        currentAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            rootView.setScaleX(scale);
            rootView.setScaleY(scale);
        });
        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        currentAnimator.start();
    }

    private void setupLogoutButton() {
        LinearLayout logoutLayout = findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void setupAboutAppButton() {
        LinearLayout aboutAppLayout = findViewById(R.id.aboutLayout);
        aboutAppLayout.setOnClickListener(v -> showAboutAppDialog());
    }

    private void setupThemeSpinner() {
        Spinner themeSpinner = findViewById(R.id.themeSpinner);
        SharedPreferences themePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedTheme = themePrefs.getInt(THEME_KEY, THEME_SYSTEM);
        themeSpinner.setSelection(savedTheme);

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyThemeWithTransition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyThemeWithTransition(int position) {
        View rootView = findViewById(android.R.id.content);

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(THEME_KEY, position);
        editor.apply();

        rootView.animate()
                .alpha(0.7f)
                .setDuration(100)
                .withEndAction(() -> {
                    switch (position) {
                        case THEME_LIGHT:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case THEME_DARK:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case THEME_SYSTEM:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }
                    new Handler().postDelayed(() -> {
                        rootView.animate()
                                .alpha(1f)
                                .setDuration(150)
                                .start();
                    }, 50);
                })
                .start();
    }

    private void setupFontSizeSpinner() {
        Spinner fontSizeSpinner = findViewById(R.id.fontSizeSpinner);
        SharedPreferences fontSizePrefs = getSharedPreferences(FONT_SIZE_PREFS, MODE_PRIVATE);
        int savedFontSize = fontSizePrefs.getInt(FONT_SIZE_KEY, FONT_SIZE_MEDIUM);
        fontSizeSpinner.setSelection(savedFontSize);

        fontSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFontSizeChange(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFontSizeChange(int position) {
        SharedPreferences.Editor editor = getSharedPreferences(FONT_SIZE_PREFS, MODE_PRIVATE).edit();
        editor.putInt(FONT_SIZE_KEY, position);
        editor.apply();
        applyFontSizeSmoothly();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja realmente sair do aplicativo?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void showAboutAppDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about_app, null);
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Fechar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        super.onDestroy();
    }
}