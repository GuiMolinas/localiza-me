package com.queridinhos.tcc;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class LocalizeActivity extends AppCompatActivity {

    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Button btnTraceRoute;
    private RouteView routeView;
    private Map<String, PointF> locations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localize);

        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        btnTraceRoute = findViewById(R.id.btn_trace_route);
        routeView = findViewById(R.id.routeView);

        // Adicionando locais de exemplo
        initializeLocations();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations.keySet().toArray(new String[0]));
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        btnTraceRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from = spinnerFrom.getSelectedItem().toString();
                String to = spinnerTo.getSelectedItem().toString();

                PointF startPoint = locations.get(from);
                PointF endPoint = locations.get(to);

                if (startPoint != null && endPoint != null) {
                    routeView.setRoute(startPoint, endPoint);
                }
            }
        });
    }

    private void initializeLocations() {
        // Coordenadas de exemplo (ajuste conforme a imagem do mapa)
        locations.put("Entrada Principal", new PointF(700f, 800f));
        locations.put("Bloco A", new PointF(854f, 552f)); //
        locations.put("Bloco A - Laboratório A", new PointF(850f, 500f));
        locations.put("Bloco B", new PointF(925f, 633f)); //
        locations.put("Bloco C - Laboratório B", new PointF(650f, 650f));
        locations.put("Biblioteca", new PointF(750f, 450f));
    }
}