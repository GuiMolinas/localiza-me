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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalizeActivity extends AppCompatActivity {

    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Button btnTraceRoute;
    private ImageButton backButton;
    private ClickableAreaDebugView clickableAreaDebugView;
    private RouteView routeView;
    private ImageView mapImageView;
    private final List<MapActivity.ClickableArea> clickableAreas = new ArrayList<>();
    private final Map<String, Path> locations = new LinkedHashMap<>();
    private final Map<String, PointF> locationCenters = new HashMap<>();

    // Mapa para armazenar os pontos de referência
    private final Map<String, PointF> routePointsMap = new HashMap<>();
    private final String HINT = "-- Selecionar --";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localize);

        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        btnTraceRoute = findViewById(R.id.btn_trace_route);
        backButton = findViewById(R.id.backButton);
        clickableAreaDebugView = findViewById(R.id.clickableAreaDebugView);
        routeView = findViewById(R.id.routeView);
        mapImageView = findViewById(R.id.mapImageView);

        initializeLocationsAndAreas();
        initializeRoutePoints(); // Carrega os pontos de rota
        setupMapMatrixListener();

        ArrayList<String> locationNames = new ArrayList<>();
        locationNames.add(HINT);
        locationNames.addAll(locations.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locationNames);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());
        btnTraceRoute.setOnClickListener(v -> traceRoute());
    }

    private void traceRoute() {
        String from = spinnerFrom.getSelectedItem().toString();
        String to = spinnerTo.getSelectedItem().toString();

        if (from.equals(HINT) || to.equals(HINT)) {
            Toast.makeText(LocalizeActivity.this, "Por favor, selecione um local de partida e um de chegada.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nova lógica de rotas fixas
        List<PointF> path = getFixedRoute(from, to);

        if (path != null && !path.isEmpty()) {
            List<PointF> fullPath = new ArrayList<>();
            fullPath.add(locationCenters.get(from)); // Ponto inicial do bloco
            fullPath.addAll(path);
            fullPath.add(locationCenters.get(to)); // Ponto final do bloco
            routeView.setRoute(fullPath);
        } else {
            Toast.makeText(this, "Rota não definida para este trajeto.", Toast.LENGTH_SHORT).show();
            routeView.clearRoute();
        }
    }

    private void initializeRoutePoints() {
        // Entradas dos Blocos
        routePointsMap.put("Bloco Alfa", new PointF(996, 480));
        routePointsMap.put("Bloco AB", new PointF(846, 552));
        routePointsMap.put("Bloco AC", new PointF(691, 584));
        routePointsMap.put("Bloco B", new PointF(879, 586));
        routePointsMap.put("Biblioteca", new PointF(789, 487));
        routePointsMap.put("Bloco CA", new PointF(692, 621));
        routePointsMap.put("Bloco CD", new PointF(581, 534));
        routePointsMap.put("Bloco D", new PointF(572, 526));
        routePointsMap.put("Bloco E", new PointF(464, 616));
        routePointsMap.put("Bloco F", new PointF(353, 698));
        routePointsMap.put("Bloco G", new PointF(317, 680));

        // Contornos e Pontos de Passagem
        routePointsMap.put("Contorno Bloco AC", new PointF(702, 616));
        routePointsMap.put("Contorno BibliotecaC", new PointF(636, 516));
        routePointsMap.put("Contorno DC", new PointF(522, 562));
        routePointsMap.put("Contorno InternoE", new PointF(431, 638));
        routePointsMap.put("Contorno AB", new PointF(722, 631));
        routePointsMap.put("Entrada Bloco Alfa", new PointF(986, 506));
        routePointsMap.put("Entrada Blocos AB", new PointF(987, 563));

        // Adicione outros pontos se necessário
    }

    private List<PointF> getFixedRoute(String from, String to) {
        String routeKey = from + " -> " + to;

        // --- ROTAS A PARTIR DO BLOCO ALFA ---
        if (from.equals("Bloco Alfa")) {
            switch (to) {
                case "Bloco D":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno BibliotecaC", "Bloco D");
                case "Bloco E":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno BibliotecaC", "Contorno DC", "Bloco E");
                case "Bloco F":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco G");
                case "Bloco C":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno Bloco AC", "Bloco CA");
                case "Bloco A":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Bloco A");
                case "Bloco B":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Bloco B");
                case "Biblioteca":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Bloco A", "Biblioteca");
                // Adicione outras rotas a partir do Bloco Alfa aqui
            }
        }

        // --- ROTAS INVERSAS PARA O BLOCO ALFA ---
        if (to.equals("Bloco Alfa")) {
            List<PointF> path = getFixedRoute(to, from); // Chama a rota normal
            if (path != null) {
                Collections.reverse(path); // Apenas inverte a ordem dos pontos
                return path;
            }
        }

        // Adicione outras combinações de rotas aqui (ex: Bloco B -> Bloco G)

        return null; // Rota não encontrada
    }

    // Método auxiliar para buscar os PointF a partir dos nomes
    private List<PointF> getPointsFor(String... names) {
        List<PointF> points = new ArrayList<>();
        for (String name : names) {
            if (routePointsMap.containsKey(name)) {
                points.add(routePointsMap.get(name));
            }
        }
        return points;
    }

    private void setupMapMatrixListener() {
        ViewTreeObserver vto = mapImageView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Matrix matrix = mapImageView.getImageMatrix();
                float zoomFactor = 1.2f;
                float verticalPan = 200f;
                float pivotX = mapImageView.getWidth() / 2f;
                float pivotY = mapImageView.getHeight() / 2f;
                matrix.postScale(zoomFactor, zoomFactor, pivotX, pivotY);
                matrix.postTranslate(0, verticalPan);
                mapImageView.setScaleType(ImageView.ScaleType.MATRIX);
                mapImageView.setImageMatrix(matrix);
                clickableAreaDebugView.setMatrix(matrix);
                routeView.setMatrix(matrix);
            }
        });
    }

    private void addArea(String name, String description, Path path) {
        clickableAreas.add(new MapActivity.ClickableArea(path, name, description));
        locations.put(name, path);
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        locationCenters.put(name, new PointF(rectF.centerX(), rectF.centerY()));
    }

    private void initializeLocationsAndAreas() {
        addArea("Bloco Alfa", "Prédio mais novo, com auditório e salas de pós-graduação.", createPath(900, 360, 958, 343, 957, 305, 1106, 257, 1156, 333, 1309, 284, 1421, 465, 1251, 521, 1289, 580, 1086, 642));
        addArea("Bloco A", "Prédio principal, com salas de aula e laboratórios de informática.", createPath(670, 530, 900, 457, 945, 523, 945, 548, 714, 617, 669, 553));
        addArea("Bloco B", "Este bloco contém as salas do curso de Direito e o Núcleo de Prática Jurídica.", createPath(737, 633, 966, 560, 1009, 626, 1009, 649, 779, 715, 734, 653));
        addArea("Bloco C", "Aqui ficam os laboratórios de saúde e as clínicas de atendimento à comunidade.", createPath(550, 577, 549, 546, 626, 522, 745, 700, 746, 716, 667, 736));
        addArea("Bloco D", "Descrição do Bloco D.", createPath(426, 438, 425, 424, 453, 414, 455, 394, 527, 372, 603, 504, 604, 523, 526, 544, 507, 518, 480, 525));
        addArea("Bloco E", "Descrição do Bloco E.", createPath(351, 628, 397, 579, 519, 641, 520, 672, 465, 716, 397, 686));
        addArea("Bloco F", "Descrição do Bloco F.", createPath(328, 725, 327, 706, 381, 679, 428, 712, 428, 730, 351, 755));
        addArea("Bloco G", "Descrição do Bloco G.", createPath(251, 660, 255, 635, 315, 614, 367, 651, 319, 673, 319, 697, 287, 705));
        addArea("Biblioteca", "Acervo de livros, salas de estudo e computadores.", createPath(623, 477, 625, 459, 842, 381, 880, 443, 879, 454, 656, 526));
    }

    private Path createPath(float... points) {
        Path path = new Path();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            path.lineTo(points[i], points[i+1]);
        }
        path.close();
        return path;
    }
}