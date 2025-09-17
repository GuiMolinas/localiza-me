package com.queridinhos.tcc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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

    /**
     * MÉTODO FINAL E CORRETO
     * Use as coordenadas de pixel EXATAS que você obtém da ferramenta de debug do app.
     * Não faça nenhum cálculo, apenas insira os números diretamente.
     */
    private void setupClickableAreas() {
        clickableAreas.clear();

        // --- INSTRUÇÕES ---
        // 1. Rode o app e anote as coordenadas X e Y de cada ponto que você tocar.
        // 2. Coloque esses números diretamente aqui, substituindo os valores de exemplo.

        // Bloco Alfa - SUBSTITUA OS VALORES ABAIXO PELOS SEUS
        Path pathAlfa = new Path();
        pathAlfa.moveTo(2359, 947);
        pathAlfa.lineTo(3415, 742);
        pathAlfa.lineTo(3734, 1215);
        pathAlfa.lineTo(3287, 1369);
        pathAlfa.lineTo(3386, 1519);
        pathAlfa.lineTo(2853, 1690);
        pathAlfa.close();
        clickableAreas.add(new ClickableArea(pathAlfa, "Bloco Alfa", "Prédio mais novo, com auditório e salas de pós-graduação."));

        // Bloco A
        Path pathA = new Path();
        pathA.moveTo(1761, 1389);
        pathA.lineTo(2361, 1193);
        pathA.lineTo(2472, 1374);
        pathA.lineTo(1872, 1619);
        pathA.close();
        clickableAreas.add(new ClickableArea(pathA, "Bloco A", "Prédio principal, com salas de aula e laboratórios de informática."));

        // Bloco B
        Path pathB = new Path();
        pathB.moveTo(1932, 1664);
        pathB.lineTo(2534, 1469);
        pathB.lineTo(2651, 1639);
        pathB.lineTo(2045, 1873);
        pathB.close();
        clickableAreas.add(new ClickableArea(pathB, "Bloco B", "Este bloco contém as salas do curso de Direito e o Núcleo de Prática Jurídica."));

        // Bloco C
        Path pathC = new Path();
        pathC.moveTo(1440, 1427);
        pathC.lineTo(1643, 1371);
        pathC.lineTo(1953, 1870);
        pathC.lineTo(1755, 1929);
        pathC.close();
        clickableAreas.add(new ClickableArea(pathC, "Bloco C", "Aqui ficam os laboratórios de saúde e as clínicas de atendimento à comunidade."));

        // Bloco D
        Path pathD = new Path();
        pathD.moveTo(1123, 1113);
        pathD.lineTo(1364, 1001);
        pathD.lineTo(1586, 1317);
        pathD.lineTo(1385, 1430);
        pathD.lineTo(1331, 1358);
        pathD.lineTo(1257, 1371);
        pathD.close();
        clickableAreas.add(new ClickableArea(pathD, "Bloco D", "Descrição do Bloco D."));

        // Bloco E
        Path pathE = new Path();
        pathE.moveTo(926, 1647);
        pathE.lineTo(1056, 1537);
        pathE.lineTo(1370, 1685);
        pathE.lineTo(1219, 1884);
        pathE.close();
        clickableAreas.add(new ClickableArea(pathE, "Bloco E", "Descrição do Bloco E."));

        // Bloco F
        Path pathF = new Path();
        pathF.moveTo(860, 1888);
        pathF.lineTo(1088, 1816);
        pathF.lineTo(1125, 1915);
        pathF.lineTo(918, 1978);
        pathF.close();
        clickableAreas.add(new ClickableArea(pathF, "Bloco F", "Descrição do Bloco F."));

        // Bloco G
        Path pathG = new Path();
        pathG.moveTo(663, 1733);
        pathG.lineTo(818, 1608);
        pathG.lineTo(926, 1694);
        pathG.lineTo(768, 1847);
        pathG.close();
        clickableAreas.add(new ClickableArea(pathG, "Bloco G", "Descrição do Bloco G."));

        // Biblioteca
        Path pathBiblioteca = new Path();
        pathBiblioteca.moveTo(1640, 1256);
        pathBiblioteca.lineTo(2215, 1021);
        pathBiblioteca.lineTo(2306, 1164);
        pathBiblioteca.lineTo(1723, 1381);
        pathBiblioteca.close();
        clickableAreas.add(new ClickableArea(pathBiblioteca, "Biblioteca", "Acervo de livros, salas de estudo e computadores."));
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