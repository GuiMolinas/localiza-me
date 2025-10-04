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
    private ImageButton backButton;
    private ClickableAreaDebugView clickableAreaDebugView;
    private RouteView routeView;
    private ImageView mapImageView;
    private final List<MapActivity.ClickableArea> clickableAreas = new ArrayList<>();
    private final Map<String, Path> locations = new LinkedHashMap<>();
    private final Map<String, PointF> locationCenters = new HashMap<>();

    // --- NOVO: Grafo de Navegação ---
    private final Map<String, Node> navigationGraph = new HashMap<>();
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
        initializeNavigationGraph(); // --- NOVO ---
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

        Node startNode = findNearestNode(locationCenters.get(from));
        Node endNode = findNearestNode(locationCenters.get(to));

        if (startNode != null && endNode != null) {

            // --- LINHA MODIFICADA AQUI ---
            // Agora passamos o "navigationGraph" para que o A* possa resetar os nós
            List<Node> path = AStar.findPath(navigationGraph, startNode, endNode);
            // -----------------------------

            if (path != null && !path.isEmpty()) {
                List<PointF> routePoints = new ArrayList<>();
                routePoints.add(locationCenters.get(from));
                for (Node node : path) {
                    routePoints.add(new PointF(node.x, node.y));
                }
                routePoints.add(locationCenters.get(to));
                routeView.setRoute(routePoints);
            } else {
                Toast.makeText(this, "Não foi possível encontrar uma rota.", Toast.LENGTH_SHORT).show();
                routeView.clearRoute(); // Limpa a rota anterior da tela
            }
        }
    }

    private Node findNearestNode(PointF point) {
        Node nearestNode = null;
        double minDistance = Double.MAX_VALUE;

        for (Node node : navigationGraph.values()) {
            double distance = Math.sqrt(Math.pow(node.x - point.x, 2) + Math.pow(node.y - point.y, 2));
            if (distance < minDistance) {
                minDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    private void initializeNavigationGraph() {
        // --- Definição dos Nós com base nas suas coordenadas ---
        Node entradaA = new Node("entradaA", 990, 565);
        Node entradaConjunto = new Node("entradaConjunto", 983, 505);
        Node entradaB = new Node("entradaB", 871, 588);
        Node entradaAB = new Node("entradaAB", 846, 570);
        Node entradaAC = new Node("entradaAC", 688, 577);
        Node entradaBiblioteca = new Node("entradaBiblioteca", 793, 478);
        Node entradaCA = new Node("entradaCA", 681, 604);
        Node entradaC = new Node("entradaC", 582, 533);
        Node entradaE = new Node("entradaE", 494, 682);
        Node entradaF = new Node("entradaF", 356, 689);
        Node entradaG = new Node("entradaG", 317, 692); // Renomeei "entrada" para "entradaG" por clareza

        // Adiciona todos ao grafo para referência
        navigationGraph.put(entradaA.id, entradaA);
        navigationGraph.put(entradaConjunto.id, entradaConjunto);
        navigationGraph.put(entradaB.id, entradaB);
        navigationGraph.put(entradaAB.id, entradaAB);
        navigationGraph.put(entradaAC.id, entradaAC);
        navigationGraph.put(entradaBiblioteca.id, entradaBiblioteca);
        navigationGraph.put(entradaCA.id, entradaCA);
        navigationGraph.put(entradaC.id, entradaC);
        navigationGraph.put(entradaE.id, entradaE);
        navigationGraph.put(entradaF.id, entradaF);
        navigationGraph.put(entradaG.id, entradaG);

        // --- Definição das Arestas (conexões lógicas entre os nós) ---
        connectNodes(entradaG, entradaF);
        connectNodes(entradaF, entradaE);
        connectNodes(entradaE, entradaCA);
        connectNodes(entradaCA, entradaC);
        connectNodes(entradaCA, entradaAC);
        connectNodes(entradaC, entradaAC);
        connectNodes(entradaAC, entradaAB);
        connectNodes(entradaAC, entradaBiblioteca);
        connectNodes(entradaAB, entradaA);
        connectNodes(entradaAB, entradaB);
        connectNodes(entradaA, entradaConjunto);
        connectNodes(entradaBiblioteca, entradaConjunto);
    }

    private void connectNodes(Node a, Node b) {
        double distance = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
        a.addNeighbor(b, distance);
        b.addNeighbor(a, distance); // Para caminhos de mão dupla
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

        Path pathA = new Path();
        pathA.moveTo(670, 530);
        pathA.lineTo(900, 457);
        pathA.lineTo(945, 523);
        pathA.lineTo(945, 548);
        pathA.lineTo(714, 617);
        pathA.lineTo(669, 553);
        pathA.close();
        addArea("Bloco A", "Prédio principal, com salas de aula e laboratórios de informática.", pathA);

        Path pathB = new Path();
        pathB.moveTo(737, 633);
        pathB.lineTo(966, 560);
        pathB.lineTo(1009, 626);
        pathB.lineTo(1009, 649);
        pathB.lineTo(779, 715);
        pathB.lineTo(734, 653);
        pathB.close();
        addArea("Bloco B", "Este bloco contém as salas do curso de Direito e o Núcleo de Prática Jurídica.", pathB);

        Path pathC = new Path();
        pathC.moveTo(550, 577);
        pathC.lineTo(549, 546);
        pathC.lineTo(626, 522);
        pathC.lineTo(745, 700);
        pathC.lineTo(746, 716);
        pathC.lineTo(667, 736);
        pathC.close();
        addArea("Bloco C", "Aqui ficam os laboratórios de saúde e as clínicas de atendimento à comunidade.", pathC);

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

        Path pathE = new Path();
        pathE.moveTo(351, 628);
        pathE.lineTo(397, 579);
        pathE.lineTo(519, 641);
        pathE.lineTo(520, 672);
        pathE.lineTo(465, 716);
        pathE.lineTo(397, 686);
        pathE.close();
        addArea("Bloco E", "Descrição do Bloco E.", pathE);

        Path pathF = new Path();
        pathF.moveTo(328, 725);
        pathF.lineTo(327, 706);
        pathF.lineTo(381, 679);
        pathF.lineTo(428, 712);
        pathF.lineTo(428, 730);
        pathF.lineTo(351, 755);
        pathF.close();
        addArea("Bloco F", "Descrição do Bloco F.", pathF);

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