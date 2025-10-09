package com.queridinhos.tcc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private PhotoView mapImageView;
    private MaterialCardView infoCard;
    private TextView infoTitle;
    private TextView infoDescription;
    private TextView debugCoordinates;
    private RelativeLayout header;
    private ClickableAreaDebugView clickableAreaDebugView;

    private final List<ClickableArea> clickableAreas = new ArrayList<>();

    public static class ClickableArea {
        private final Path path;
        private final Region region;
        final String name;
        final String description;

        ClickableArea(Path path, String name, String description) {
            this.path = path;
            this.name = name;
            this.description = description;

            RectF rectF = new RectF();
            path.computeBounds(rectF, true);
            this.region = new Region();
            this.region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }

        public boolean contains(float x, float y) {
            return region.contains((int) x, (int) y);
        }

        public Path getPath() {
            return path;
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
        clickableAreaDebugView = findViewById(R.id.clickableAreaDebugView);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        mapImageView.setMinimumScale(1.0f);
        mapImageView.setMaximumScale(4.0f);

        // ATIVA/DESATIVA A VISIBILIDADE DA CAMADA DE DEBUG (ÁREAS DE CLIQUE)
        SharedPreferences prefs = getSharedPreferences("VisualizarCliquePrefs", MODE_PRIVATE);
        boolean visualizarClique = prefs.getBoolean("visualizarClique", false);
        clickableAreaDebugView.setVisibility(visualizarClique ? View.VISIBLE : View.GONE);

        // ATIVA/DESATIVA A VISIBILIDADE DAS COORDENADAS DE DEBUG (TEXTO X, Y)
        SharedPreferences coordsPrefs = getSharedPreferences("CoordenadasPrefs", MODE_PRIVATE);
        boolean visualizarCoordenadas = coordsPrefs.getBoolean("visualizarCoordenadas", false);
        debugCoordinates.setVisibility(visualizarCoordenadas ? View.VISIBLE : View.GONE);


        setupClickableAreas();
        animateHeader();

        mapImageView.setOnMatrixChangeListener(rect -> {
            if (clickableAreaDebugView != null) {
                clickableAreaDebugView.setMatrix(mapImageView.getImageMatrix());
            }
        });

        mapImageView.setOnViewTapListener((view, x, y) -> {
            float[] touchPoint = { x, y };
            Matrix inverseMatrix = new Matrix();
            if (mapImageView.getImageMatrix() != null) {
                mapImageView.getImageMatrix().invert(inverseMatrix);
                inverseMatrix.mapPoints(touchPoint);
                float imageX = touchPoint[0];
                float imageY = touchPoint[1];

                String coordsText = String.format(Locale.US, "X: %.1f, Y: %.1f", imageX, imageY);
                debugCoordinates.setText(coordsText);

                boolean areaClicked = false;
                for (ClickableArea area : clickableAreas) {
                    if (area.contains(imageX, imageY)) {
                        showInfoCard(area.name, area.description);
                        areaClicked = true;
                        break;
                    }
                }

                if (!areaClicked) {
                    hideInfoCard();
                }
            }
        });
    }

    private void animateHeader() {
        header.setAlpha(0f);
        header.setTranslationY(-150);
        header.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300)
                .start();
    }

    private void setupClickableAreas() {
        clickableAreas.clear();

        // Bloco Alfa
        Path pathAlfa = new Path();
        pathAlfa.moveTo(900, 360);
        pathAlfa.lineTo(958, 343);
        pathAlfa.lineTo(957, 305);
        pathAlfa.lineTo(1106, 257);
        pathAlfa.lineTo(1156, 333);
        pathAlfa.lineTo(1309, 284);
        pathAlfa.lineTo(1421, 465);
        pathAlfa.lineTo(1251, 521);
        pathAlfa.lineTo(1289, 580);
        pathAlfa.lineTo(1086, 642);
        pathAlfa.close();
        clickableAreas.add(new ClickableArea(pathAlfa, "Bloco Alfa", "Prédio mais novo, com auditório e salas de pós-graduação."));

        // Bloco A
        Path pathA = new Path();
        pathA.moveTo(670, 530);
        pathA.lineTo(900, 457);
        pathA.lineTo(945, 523);
        pathA.lineTo(945, 548);
        pathA.lineTo(714, 617);
        pathA.lineTo(669, 553);
        pathA.close();
        clickableAreas.add(new ClickableArea(pathA, "Bloco A", "Prédio principal, com salas de aula e laboratórios de informática."));

        // Bloco B
        Path pathB = new Path();
        pathB.moveTo(737, 633);
        pathB.lineTo(966, 560);
        pathB.lineTo(1009, 626);
        pathB.lineTo(1009, 649);
        pathB.lineTo(779, 715);
        pathB.lineTo(734, 653);
        pathB.close();
        clickableAreas.add(new ClickableArea(pathB, "Bloco B", "Este bloco contém as salas do curso de Direito e o Núcleo de Prática Jurídica."));

        // Bloco C
        Path pathC = new Path();
        pathC.moveTo(550, 577);
        pathC.lineTo(549, 546);
        pathC.lineTo(626, 522);
        pathC.lineTo(745, 700);
        pathC.lineTo(746, 716);
        pathC.lineTo(667, 736);
        pathC.close();
        clickableAreas.add(new ClickableArea(pathC, "Bloco C", "Aqui ficam os laboratórios de saúde e as clínicas de atendimento à comunidade."));

        // Bloco D
        Path pathD = new Path();
        pathD.moveTo(426, 438);
        pathD.lineTo(425, 424);
        pathD.lineTo(453, 414);
        pathD.lineTo(455, 394);
        pathD.lineTo(527, 372);
        pathD.lineTo(603, 504);
        pathD.lineTo(604, 523);
        pathD.lineTo(526, 544);
        pathD.lineTo(507, 518);
        pathD.lineTo(480, 525);
        pathD.close();
        clickableAreas.add(new ClickableArea(pathD, "Bloco D", "Descrição do Bloco D."));

        // Bloco E
        Path pathE = new Path();
        pathE.moveTo(351, 628);
        pathE.lineTo(397, 579);
        pathE.lineTo(519, 641);
        pathE.lineTo(520, 672);
        pathE.lineTo(465, 716);
        pathE.lineTo(397, 686);
        pathE.close();
        clickableAreas.add(new ClickableArea(pathE, "Bloco E", "Descrição do Bloco E."));

        // Bloco F
        Path pathF = new Path();
        pathF.moveTo(328, 725);
        pathF.lineTo(327, 706);
        pathF.lineTo(381, 679);
        pathF.lineTo(428, 712);
        pathF.lineTo(428, 730);
        pathF.lineTo(351, 755);
        pathF.close();
        clickableAreas.add(new ClickableArea(pathF, "Bloco F", "Descrição do Bloco F."));

        // Bloco G
        Path pathG = new Path();
        pathG.moveTo(251, 660);
        pathG.lineTo(255, 635);
        pathG.lineTo(315, 614);
        pathG.lineTo(367, 651);
        pathG.lineTo(319, 673);
        pathG.lineTo(319, 697);
        pathG.lineTo(287, 705);
        pathG.close();
        clickableAreas.add(new ClickableArea(pathG, "Bloco G", "Descrição do Bloco G."));

        // Biblioteca
        Path pathBiblioteca = new Path();
        pathBiblioteca.moveTo(623, 477);
        pathBiblioteca.lineTo(625, 459);
        pathBiblioteca.lineTo(842, 381);
        pathBiblioteca.lineTo(880, 443);
        pathBiblioteca.lineTo(879, 454);
        pathBiblioteca.lineTo(656, 526);
        pathBiblioteca.close();
        clickableAreas.add(new ClickableArea(pathBiblioteca, "Biblioteca", "Acervo de livros, salas de estudo e computadores."));

        clickableAreaDebugView.setClickableAreas(clickableAreas);
    }

    private void showInfoCard(String title, String description) {
        infoTitle.setText(title);
        infoDescription.setText(description);
        if (infoCard.getVisibility() == View.GONE) {
            infoCard.setAlpha(0f);
            infoCard.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(infoCard, "alpha", 0f, 1f).setDuration(300).start();
        }
    }

    private void hideInfoCard() {
        if (infoCard.getVisibility() == View.VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(infoCard, "alpha", 1f, 0f);
            animator.setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    infoCard.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }
}