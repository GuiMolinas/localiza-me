package com.queridinhos.tcc;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    // Constantes para temas
    protected static final int THEME_LIGHT = 0;
    protected static final int THEME_DARK = 1;
    protected static final int THEME_SYSTEM = 2;

    // Constantes para tamanho de fontes
    protected static final int FONT_SIZE_SMALL = 0;
    protected static final int FONT_SIZE_MEDIUM = 1;
    protected static final int FONT_SIZE_LARGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplica o tema antes de criar as views
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        // Pré-carrega as dimensões de fonte para melhor performance
        preloadFontSizes();
    }

    private void preloadFontSizes() {
        // Carrega antecipadamente para evitar lag
        getResources().getDimension(R.dimen.text_size_small);
        getResources().getDimension(R.dimen.text_size_medium);
        getResources().getDimension(R.dimen.text_size_large);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Aplica o tamanho da fonte com animação suave
        applyFontSizeSmoothly();
    }

    protected void applyFontSizeSmoothly() {
        SharedPreferences prefs = getSharedPreferences("FontSizePrefs", MODE_PRIVATE);
        int fontSize = prefs.getInt("selectedFontSize", FONT_SIZE_MEDIUM);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            animateFontSizeChange(rootView, fontSize);
        }
    }

    private void animateFontSizeChange(View rootView, int fontSize) {
        ValueAnimator animator = ValueAnimator.ofFloat(0.9f, 1f);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            rootView.setScaleX(progress);
            rootView.setScaleY(progress);
            if (progress > 0.95f) {
                applyFontSizeImmediately(rootView, fontSize);
            }
        });
        animator.start();
    }

    private void applyFontSizeImmediately(View view, int fontSize) {
        try {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    if (child != null) {
                        applyFontSizeImmediately(child, fontSize);
                    }
                }
            } else if (view instanceof TextView) {
                TextView textView = (TextView) view;
                float textSize = getFontSizeForOption(fontSize);

                // Aplica com pequena animação para suavizar
                textView.animate()
                        .scaleX(0.98f).scaleY(0.98f)
                        .setDuration(50)
                        .withEndAction(() -> {
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                            textView.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        })
                        .start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float getFontSizeForOption(int fontSize) {
        switch (fontSize) {
            case FONT_SIZE_SMALL:
                return getResources().getDimension(R.dimen.text_size_small);
            case FONT_SIZE_LARGE:
                return getResources().getDimension(R.dimen.text_size_large);
            case FONT_SIZE_MEDIUM:
            default:
                return getResources().getDimension(R.dimen.text_size_medium);
        }
    }

    @Override
    public void onClick(View v) {
        // Implementação padrão vazia
    }

    // Método para forçar atualização do tema sem recriar a Activity
    protected void updateTheme(int themeMode) {
        new Handler().postDelayed(() -> {
            switch (themeMode) {
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
        }, 50);
    }
}