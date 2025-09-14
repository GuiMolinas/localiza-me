package com.queridinhos.tcc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout; // Alterado de LinearLayout
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private PhotoView mapImageView;
    private MaterialCardView infoCard;
    private TextView infoTitle;
    private TextView infoDescription;
    private TextView debugCoordinates;
    private RelativeLayout header; // Alterado de LinearLayout


    private final List<ClickableArea> clickableAreas = new ArrayList<>();

    private static class ClickableArea {
        final RectF bounds;
        final String name;
        final String description;

        ClickableArea(RectF bounds, String name, String description) {
            this.bounds = bounds;
            this.name = name;
            this.description = description;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapImageView = findViewById(R.id.mapImageView);
        infoCard = findViewById(R.id.infoCard);
        infoTitle = findViewById(R.id.infoTitle);
        infoDescription = findViewById(R.id.infoDescription);
        debugCoordinates = findViewById(R.id.debugCoordinates);
        header = findViewById(R.id.header);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());


        mapImageView.setMinimumScale(1.0f);
        mapImageView.setMaximumScale(4.0f);

        setupClickableAreas();
        animateHeader();

        mapImageView.setOnViewTapListener((view, x, y) -> {
            float[] touchPoint = { x, y };
            Matrix inverseMatrix = new Matrix();
            mapImageView.getImageMatrix().invert(inverseMatrix);
            inverseMatrix.mapPoints(touchPoint);
            float imageX = touchPoint[0];
            float imageY = touchPoint[1];

            String coordsText = String.format(Locale.US, "X: %.1f, Y: %.1f", imageX, imageY);
            debugCoordinates.setText(coordsText);

            boolean areaClicked = false;
            for (ClickableArea area : clickableAreas) {
                if (area.bounds.contains(imageX, imageY)) {
                    showInfoCard(area.name, area.description);
                    areaClicked = true;
                    break;
                }
            }

            if (!areaClicked) {
                hideInfoCard();
            }
        });
    }

    private void animateHeader() {
        header.setAlpha(0f);
        header.setTranslationY(-header.getHeight());
        header.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300)
                .start();
    }


    private void setupClickableAreas() {
        // As áreas foram expandidas para cobrir toda a construção, facilitando o clique.
        // Formato: new RectF(left, top, right, bottom)

        // Bloco A (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(1750, 1250, 2500, 1500),
                "Bloco A", "Prédio principal, com salas de aula e laboratórios de informática."
        ));

        // Bloco B (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(1900, 1550, 2700, 1800),
                "Bloco B", "Este bloco contém as salas do curso de Direito e o Núcleo de Prática Jurídica."
        ));

        // Bloco C (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(1400, 1500, 2000, 1900),
                "Bloco C", "Aqui ficam os laboratórios de saúde e as clínicas de atendimento à comunidade."
        ));

        // Bloco D (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(1100, 1100, 1600, 1400),
                "Bloco D", "Descrição do Bloco D."
        ));

        // Bloco E (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(900, 1600, 1400, 1800),
                "Bloco E", "Descrição do Bloco E."
        ));

        // Bloco F (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(850, 1850, 1150, 1950),
                "Bloco F", "Descrição do Bloco F."
        ));

        // Bloco G (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(650, 1650, 1000, 1800),
                "Bloco G", "Descrição do Bloco G."
        ));

        // Bloco Alfa (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(2500, 900, 3850, 1350),
                "Bloco Alfa", "Prédio mais novo, com auditório e salas de pós-graduação."
        ));

        // Biblioteca (Expandido)
        clickableAreas.add(new ClickableArea(
                new RectF(1600, 1150, 2350, 1300),
                "Biblioteca", "Acervo de livros, salas de estudo e computadores."
        ));
    }

    private void showInfoCard(String title, String description) {
        infoTitle.setText(title);
        infoDescription.setText(description);
        if (infoCard.getVisibility() == View.GONE) {
            infoCard.setAlpha(0f);
            infoCard.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(infoCard, "alpha", 0f, 1f);
            fadeIn.setDuration(300);
            fadeIn.start();
        }
    }

    private void hideInfoCard() {
        if (infoCard.getVisibility() == View.VISIBLE) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(infoCard, "alpha", 1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    infoCard.setVisibility(View.GONE);
                }
            });
            fadeOut.start();
        }
    }
}