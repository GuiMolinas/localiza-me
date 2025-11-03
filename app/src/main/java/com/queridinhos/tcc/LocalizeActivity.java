package com.queridinhos.tcc;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
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

        if (from.equals(to)) {
            Toast.makeText(LocalizeActivity.this, "Por favor, selecione locais distintos.", Toast.LENGTH_SHORT).show();
            routeView.clearRoute(); // Limpa qualquer rota anterior
            return;
        }

        List<PointF> path = getFixedRoute(from, to);

        if (path != null && !path.isEmpty()) {
            List<PointF> fullPath = new ArrayList<>();
            fullPath.add(locationCenters.get(from)); // Ponto inicial do bloco
            fullPath.addAll(path);
            fullPath.add(locationCenters.get(to)); // Ponto final do bloco

            // ***** INÍCIO DA MODIFICAÇÃO *****
            routeView.setRoute(fullPath, () -> {
                // Este código é executado QUANDO a animação da rota termina.
                Toast.makeText(LocalizeActivity.this, "Você chegou ao seu destino!", Toast.LENGTH_SHORT).show();

                // VERIFICA SE O BLOCO DE DESTINO TEM MAPA INTERNO
                if (to.equals("Bloco Alfa") || to.equals("Bloco F")) {
                    // Se for Bloco Alfa ou F, apenas mostra o aviso após um pequeno delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Toast.makeText(LocalizeActivity.this, R.string.internal_map_unavailable, Toast.LENGTH_LONG).show();
                    }, 1500); // 1.5 segundos de delay
                } else if (!to.equals("Biblioteca")) {
                    // Se for qualquer outro bloco (exceto Biblioteca), abre o mapa interno
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(LocalizeActivity.this, InternalMapActivity.class);
                        intent.putExtra("BLOCK_NAME", to);
                        startActivity(intent);
                    }, 2000); // 2 segundos de delay
                }
                // Se for a Biblioteca, não faz nada (como na lógica original)
            });
            // ***** FIM DA MODIFICAÇÃO *****

        } else {
            Toast.makeText(this, "Rota não definida para este trajeto.", Toast.LENGTH_SHORT).show();
            routeView.clearRoute();
        }
    }

    private void initializeRoutePoints() {
        // Entradas dos Blocos
        routePointsMap.put("Bloco Alfa", new PointF(996, 480));
        routePointsMap.put("Bloco A", new PointF(807, 538)); // Ponto central para Bloco A
        routePointsMap.put("Bloco B", new PointF(879, 586));
        routePointsMap.put("Bloco C", new PointF(648, 640)); // Ponto central para Bloco C
        routePointsMap.put("Bloco D", new PointF(572, 526));
        routePointsMap.put("Bloco E", new PointF(464, 616));
        routePointsMap.put("Bloco F", new PointF(353, 698));
        routePointsMap.put("Bloco G", new PointF(317, 680));
        routePointsMap.put("Biblioteca", new PointF(789, 487));

        // Entradas específicas (se necessário para lógica, mas os centros acima são melhores para rotas)
        routePointsMap.put("Bloco AB", new PointF(846, 552));
        routePointsMap.put("Bloco AC", new PointF(691, 584));
        routePointsMap.put("Bloco CA", new PointF(692, 621));
        routePointsMap.put("Bloco CD", new PointF(581, 534));


        // Contornos e Pontos de Passagem
        routePointsMap.put("Contorno Bloco AC", new PointF(702, 616));
        routePointsMap.put("Contorno BibliotecaC", new PointF(636, 516));
        routePointsMap.put("Contorno DC", new PointF(522, 562));
        routePointsMap.put("Contorno InternoE", new PointF(431, 638));
        routePointsMap.put("Contorno AB", new PointF(821, 596));
        routePointsMap.put("Entrada Bloco Alfa", new PointF(986, 506));
        routePointsMap.put("Entrada Blocos AB", new PointF(987, 563));
    }

    private List<PointF> getFixedRoute(String from, String to) {

        // --- ROTAS A PARTIR DO BLOCO ALFA ---
        if (from.equals("Bloco Alfa")) {
            switch (to) {
                case "Bloco D":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Bloco D");
                case "Bloco E":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E");
                case "Bloco F":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco G");
                case "Bloco C":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Contorno AB", "Contorno Bloco AC", "Bloco CA");
                case "Bloco A":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Bloco A");
                case "Bloco B":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Bloco B");
                case "Biblioteca":
                    return getPointsFor("Entrada Bloco Alfa", "Entrada Blocos AB", "Bloco A", "Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO A ---
        if (from.equals("Bloco A")) {
            switch (to) {
                case "Bloco D":
                    return getPointsFor("Bloco AC", "Contorno BibliotecaC", "Bloco D");
                case "Bloco E":
                    return getPointsFor("Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E");
                case "Bloco F":
                    return getPointsFor("Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco G");
                case "Bloco C":
                    return getPointsFor("Bloco AC", "Bloco CA");
                case "Bloco B":
                    return getPointsFor("Bloco B");
                case "Biblioteca":
                    return getPointsFor("Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO B ---
        if (from.equals("Bloco B")) {
            switch (to) {
                case "Bloco D":
                    return getPointsFor("Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Bloco D");
                case "Bloco E":
                    return getPointsFor("Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E");
                case "Bloco F":
                    return getPointsFor("Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Contorno AB", "Contorno Bloco AC", "Contorno BibliotecaC", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco G");
                case "Bloco C":
                    return getPointsFor("Contorno AB", "Contorno Bloco AC", "Bloco CA");
                case "Biblioteca":
                    return getPointsFor("Bloco A", "Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO C ---
        if (from.equals("Bloco C")) {
            switch (to) {
                case "Bloco D":
                    return getPointsFor("Bloco CD", "Bloco D");
                case "Bloco E":
                    return getPointsFor("Bloco CD", "Contorno DC", "Bloco E");
                case "Bloco F":
                    return getPointsFor("Bloco CD", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Bloco CD", "Contorno DC", "Bloco E", "Contorno InternoE", "Bloco G");
                case "Biblioteca":
                    return getPointsFor("Bloco CA", "Bloco AC", "Bloco A", "Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO D ---
        if (from.equals("Bloco D")) {
            switch (to) {
                case "Bloco E":
                    return getPointsFor("Contorno DC", "Bloco E");
                case "Bloco F":
                    return getPointsFor("Contorno DC", "Bloco E", "Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Contorno DC", "Bloco E", "Contorno InternoE", "Bloco G");
                case "Biblioteca":
                    return getPointsFor("Contorno BibliotecaC", "Bloco AC", "Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO E ---
        if (from.equals("Bloco E")) {
            switch (to) {
                case "Bloco F":
                    return getPointsFor("Contorno InternoE", "Bloco F");
                case "Bloco G":
                    return getPointsFor("Contorno InternoE", "Bloco G");
                case "Biblioteca":
                    return getPointsFor("Contorno DC", "Contorno BibliotecaC", "Bloco AC", "Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO F ---
        if (from.equals("Bloco F")) {
            switch (to) {
                case "Bloco G":
                    return getPointsFor("Bloco G");
                case "Biblioteca":
                    return getPointsFor("Contorno InternoE", "Bloco E", "Contorno DC", "Contorno BibliotecaC", "Bloco AC", "Biblioteca");
            }
        }

        // --- ROTAS A PARTIR DO BLOCO G ---
        if (from.equals("Bloco G")) {
            switch (to) {
                case "Biblioteca":
                    return getPointsFor("Contorno InternoE", "Bloco E", "Contorno DC", "Contorno BibliotecaC", "Bloco AC", "Biblioteca");
            }
        }


        // --- ROTAS INVERSAS ---
        // Se a rota direta não foi encontrada, tenta a inversa
        List<PointF> reversePath = getFixedRoute(to, from);
        if (reversePath != null) {
            Collections.reverse(reversePath);
            return reversePath;
        }

        return null; // Rota não encontrada em nenhuma direção
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