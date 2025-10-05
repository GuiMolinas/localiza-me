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
        initializeNavigationGraph();
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
            List<Node> path = AStar.findPath(navigationGraph, startNode, endNode);

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
                routeView.clearRoute();
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
        // --- NÓS ---
        Node entradaA = new Node("entradaA", 990, 565);
        Node entradaAlfa = new Node("entradaAlfa", 983, 505);
        Node entradaB = new Node("entradaB", 871, 588);
        Node entradaBiblioteca = new Node("entradaBiblioteca", 793, 478);
        Node entradaC = new Node("entradaC", 582, 533);
        Node entradaD = new Node("entradaD", 572, 529);
        Node entradaE = new Node("entradaE", 494, 682);
        Node entradaF = new Node("entradaF", 356, 689);
        Node entradaG = new Node("entradaG", 317, 692);
        Node entradaAB = new Node("entradaAB", 846, 570);
        Node entradaAC = new Node("entradaAC", 688, 577);
        Node entradaCA = new Node("entradaCA", 681, 604);
        Node contornoAC = new Node("contornoAC", 707, 624);
        Node contornoCD = new Node("contornoCD", 614, 513);
        Node contornoCE = new Node("contornoCE", 561, 652);
        Node contornoE = new Node("contornoE", 432, 620);
        Node contornoGF = new Node("contornoGF", 329, 688);
        Node corredorA = new Node("corredorA", 870, 515);
        Node desvioE = new Node("desvioE", 525, 615);
        Node contornoDBiblioteca = new Node("contornoDBiblioteca", 628, 513);

        // Adicionar TODOS os nós ao grafo
        navigationGraph.put(entradaA.id, entradaA);
        navigationGraph.put(entradaAlfa.id, entradaAlfa);
        navigationGraph.put(entradaB.id, entradaB);
        navigationGraph.put(entradaBiblioteca.id, entradaBiblioteca);
        navigationGraph.put(entradaC.id, entradaC);
        navigationGraph.put(entradaD.id, entradaD);
        navigationGraph.put(entradaE.id, entradaE);
        navigationGraph.put(entradaF.id, entradaF);
        navigationGraph.put(entradaG.id, entradaG);
        navigationGraph.put(entradaAB.id, entradaAB);
        navigationGraph.put(entradaAC.id, entradaAC);
        navigationGraph.put(entradaCA.id, entradaCA);
        navigationGraph.put(contornoAC.id, contornoAC);
        navigationGraph.put(contornoCD.id, contornoCD);
        navigationGraph.put(contornoCE.id, contornoCE);
        navigationGraph.put(contornoE.id, contornoE);
        navigationGraph.put(contornoGF.id, contornoGF);
        navigationGraph.put(corredorA.id, corredorA);
        navigationGraph.put(desvioE.id, desvioE);
        navigationGraph.put(contornoDBiblioteca.id, contornoDBiblioteca);

        // --- CONEXÕES LÓGICAS FINAIS COM A ÚLTIMA REGRA ---

        // REGRA: Saída do Alfa é SOMENTE pelo Bloco A.
        connectNodes(entradaAlfa, entradaA);

        // Conexões do lado DIREITO do campus (A, B)
        connectNodes(entradaA, entradaAB);
        connectNodes(entradaB, entradaAB);

        // REGRA: Caminho Alfa -> Biblioteca (passando por DENTRO de A)
        connectNodes(entradaA, corredorA);
        connectNodes(corredorA, entradaBiblioteca);

        // Conexão da Biblioteca com o "hub" superior
        connectNodes(entradaBiblioteca, contornoCD);

        // "Hub" Superior (SÓ TEM SAÍDA PARA C E PARA O PONTO OBRIGATÓRIO)
        connectNodes(contornoCD, entradaC);
        connectNodes(contornoCD, contornoDBiblioteca); // Conexão para o novo ponto obrigatório

        // REGRA: Para ir para D, E, F, G, deve passar pelo novo nó.
        connectNodes(contornoDBiblioteca, entradaD); // <<< NOVA CONEXÃO AQUI
        connectNodes(contornoDBiblioteca, desvioE);  // Conexão para o resto da esquerda

        // O resto do caminho para a esquerda (como antes).
        connectNodes(desvioE, contornoE);
        connectNodes(contornoE, entradaE);
        connectNodes(contornoE, contornoGF);
        connectNodes(contornoGF, entradaF);
        connectNodes(contornoGF, entradaG);

        // Caminhos SECUNDÁRIOS por baixo (para quem vem do B, por exemplo)
        connectNodes(entradaAB, contornoAC);
        connectNodes(contornoAC, entradaCA);
        connectNodes(entradaCA, contornoCE);
        connectNodes(contornoCE, entradaC);
        connectNodes(contornoCE, desvioE);
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