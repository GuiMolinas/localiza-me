package com.queridinhos.tcc;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalizeActivity extends AppCompatActivity {

    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Button btnTraceRoute;
    private ImageButton backButton; // Botão de voltar
    private ClickableAreaDebugView clickableAreaDebugView;
    private RouteView routeView;
    private ImageView mapImageView;
    private final List<MapActivity.ClickableArea> clickableAreas = new ArrayList<>();
    private final Map<String, Path> locations = new LinkedHashMap<>();
    private final Map<String, PointF> locationCenters = new HashMap<>();
    private final String HINT = "-- Selecionar --";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localize);

        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        btnTraceRoute = findViewById(R.id.btn_trace_route);
        backButton = findViewById(R.id.backButton); // Inicializa o botão
        clickableAreaDebugView = findViewById(R.id.clickableAreaDebugView);
        routeView = findViewById(R.id.routeView);
        mapImageView = findViewById(R.id.mapImageView);

        initializeLocationsAndAreas();
        setupMapMatrixListener(); // Configura o listener para a matriz

        ArrayList<String> locationNames = new ArrayList<>();
        locationNames.add(HINT);
        locationNames.addAll(locations.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locationNames);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Ação do botão de voltar
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Fecha a atividade atual
            }
        });

        btnTraceRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from = spinnerFrom.getSelectedItem().toString();
                String to = spinnerTo.getSelectedItem().toString();

                if (from.equals(HINT) || to.equals(HINT)) {
                    Toast.makeText(LocalizeActivity.this, "Por favor, selecione um local de partida e um de chegada.", Toast.LENGTH_SHORT).show();
                    return;
                }

                MapActivity.ClickableArea fromArea = findAreaByName(from);
                MapActivity.ClickableArea toArea = findAreaByName(to);

                if (fromArea != null && toArea != null) {
                    List<MapActivity.ClickableArea> areasToHighlight = new ArrayList<>();
                    areasToHighlight.add(fromArea);
                    areasToHighlight.add(toArea);
                    clickableAreaDebugView.setClickableAreas(areasToHighlight);
                }

                PointF fromPoint = locationCenters.get(from);
                PointF toPoint = locationCenters.get(to);

                if (fromPoint != null && toPoint != null) {
                    routeView.setRoute(fromPoint, toPoint);
                }
            }
        });
    }

    private void setupMapMatrixListener() {
        ViewTreeObserver vto = mapImageView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Obtém a matriz original calculada pelo fitCenter
                Matrix matrix = mapImageView.getImageMatrix();

                // --- MODIFICAÇÃO PARA ZOOM E PAN INICIAL ---
                // Fator de zoom (1.0f = sem zoom, 1.2f = 20% de zoom)
                float zoomFactor = 1.2f;
                // Deslocamento vertical em pixels (valores positivos descem o mapa)
                float verticalPan = 200f;

                // Calcula o ponto central da view para o zoom
                float pivotX = mapImageView.getWidth() / 2f;
                float pivotY = mapImageView.getHeight() / 2f;

                // Aplica o zoom e o deslocamento na matriz
                matrix.postScale(zoomFactor, zoomFactor, pivotX, pivotY);
                matrix.postTranslate(0, verticalPan);

                // Define que o ImageView usará nossa matriz customizada
                mapImageView.setScaleType(ImageView.ScaleType.MATRIX);
                mapImageView.setImageMatrix(matrix);
                // --- FIM DA MODIFICAÇÃO ---

                // Passa a matriz MODIFICADA para as views de desenho
                clickableAreaDebugView.setMatrix(matrix);
                routeView.setMatrix(matrix);
            }
        });
    }

    private MapActivity.ClickableArea findAreaByName(String name) {
        for (MapActivity.ClickableArea area : clickableAreas) {
            if (area.name.equals(name)) {
                return area;
            }
        }
        return null;
    }

    private void addArea(String name, String description, Path path) {
        clickableAreas.add(new MapActivity.ClickableArea(path, name, description));
        locations.put(name, path);

        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        locationCenters.put(name, new PointF(rectF.centerX(), rectF.centerY()));
    }

    private void initializeLocationsAndAreas() {
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
        addArea("Bloco Alfa", "Prédio mais novo, com auditório e salas de pós-graduação.", pathAlfa);

        // Bloco A
        Path pathA = new Path();
        pathA.moveTo(670, 530);
        pathA.lineTo(900, 457);
        pathA.lineTo(945, 523);
        pathA.lineTo(945, 548);
        pathA.lineTo(714, 617);
        pathA.lineTo(669, 553);
        pathA.close();
        addArea("Bloco A", "Prédio principal, com salas de aula e laboratórios de informática.", pathA);

        // Bloco B
        Path pathB = new Path();
        pathB.moveTo(737, 633);
        pathB.lineTo(966, 560);
        pathB.lineTo(1009, 626);
        pathB.lineTo(1009, 649);
        pathB.lineTo(779, 715);
        pathB.lineTo(734, 653);
        pathB.close();
        addArea("Bloco B", "Este bloco contém as salas do curso de Direito e o Núcleo de Prática Jurídica.", pathB);

        // Bloco C
        Path pathC = new Path();
        pathC.moveTo(550, 577);
        pathC.lineTo(549, 546);
        pathC.lineTo(626, 522);
        pathC.lineTo(745, 700);
        pathC.lineTo(746, 716);
        pathC.lineTo(667, 736);
        pathC.close();
        addArea("Bloco C", "Aqui ficam os laboratórios de saúde e as clínicas de atendimento à comunidade.", pathC);

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
        addArea("Bloco D", "Descrição do Bloco D.", pathD);

        // Bloco E
        Path pathE = new Path();
        pathE.moveTo(351, 628);
        pathE.lineTo(397, 579);
        pathE.lineTo(519, 641);
        pathE.lineTo(520, 672);
        pathE.lineTo(465, 716);
        pathE.lineTo(397, 686);
        pathE.close();
        addArea("Bloco E", "Descrição do Bloco E.", pathE);

        // Bloco F
        Path pathF = new Path();
        pathF.moveTo(328, 725);
        pathF.lineTo(327, 706);
        pathF.lineTo(381, 679);
        pathF.lineTo(428, 712);
        pathF.lineTo(428, 730);
        pathF.lineTo(351, 755);
        pathF.close();
        addArea("Bloco F", "Descrição do Bloco F.", pathF);

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
        addArea("Bloco G", "Descrição do Bloco G.", pathG);

        // Biblioteca
        Path pathBiblioteca = new Path();
        pathBiblioteca.moveTo(623, 477);
        pathBiblioteca.lineTo(625, 459);
        pathBiblioteca.lineTo(842, 381);
        pathBiblioteca.lineTo(880, 443);
        pathBiblioteca.lineTo(879, 454);
        pathBiblioteca.lineTo(656, 526);
        pathBiblioteca.close();
        addArea("Biblioteca", "Acervo de livros, salas de estudo e computadores.", pathBiblioteca);
    }
}