package com.queridinhos.tcc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.expressions.generated.Expression;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.RasterLayer;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
// A importação correta que precisamos
import com.mapbox.maps.extension.style.sources.generated.ImageSource;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private GeoJsonSource poiSource;
    private MaterialCardView infoCard;
    private TextView infoTitle;
    private TextView infoDescription;

    private static final String IMAGE_SOURCE_ID = "map-image-source";
    private static final String POI_SOURCE_ID = "poi-source";
    private static final String POI_SELECTED_LAYER_ID = "poi-selected-layer";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_DESC = "description";

    private FeatureCollection featureCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        infoCard = findViewById(R.id.infoCard);
        infoTitle = findViewById(R.id.infoTitle);
        infoDescription = findViewById(R.id.infoDescription);

        mapboxMap = mapView.getMapboxMap();
        mapboxMap.loadStyleUri(Style.LIGHT, this::onStyleLoaded);
    }

    private void onStyleLoaded(Style style) {
        setupMapImage(style);
        setupPOIs(style);
        setupClickListener();
    }

    private void setupMapImage(Style style) {
        double mapWidth = 1598.0;
        double mapHeight = 950.0;

        // A lista de coordenadas DEVE ser do tipo Point.
        List<Point> coordinates = Arrays.asList(
                Point.fromLngLat(0, mapHeight),
                Point.fromLngLat(mapWidth, mapHeight),
                Point.fromLngLat(mapWidth, 0),
                Point.fromLngLat(0, 0)
        );

        // ✅ ESTA É A ABORDAGEM CORRETA E FINAL
        // 1. Crie a ImageSource APENAS com as coordenadas.
        ImageSource imageSource = new ImageSource.Builder(IMAGE_SOURCE_ID)
                .coordinates(coordinates)
                .build();
        style.addSource(imageSource);

        // 2. Crie a camada que usará a fonte.
        RasterLayer rasterLayer = new RasterLayer("map-image-layer", IMAGE_SOURCE_ID);
        style.addLayer(rasterLayer);

        // 3. Carregue o Bitmap.
        // Lembre-se de usar o nome correto do seu arquivo. Se for "mapa-atualizado.jpg", use R.drawable.mapa_atualizado
        Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapa);

        // 4. ATUALIZE a fonte que JÁ EXISTE no estilo.
        ImageSource sourceFromStyle = (ImageSource) style.getSource(IMAGE_SOURCE_ID);
        if (sourceFromStyle != null) {
            sourceFromStyle.updateImage(mapBitmap);
        }
    }


    private void setupPOIs(Style style) {
        List<Feature> features = new ArrayList<>();

        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(460, 480), Point.fromLngLat(580, 480),
                                Point.fromLngLat(580, 360), Point.fromLngLat(460, 360),
                                Point.fromLngLat(460, 480)
                        ))),
                createPoiProperties("Bloco A", "Prédio principal com salas de aula e laboratórios.")
        ));

        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(360, 560), Point.fromLngLat(450, 560),
                                Point.fromLngLat(450, 510), Point.fromLngLat(360, 510),
                                Point.fromLngLat(360, 560)
                        ))),
                createPoiProperties("Biblioteca", "Acervo de livros, salas de estudo e computadores.")
        ));

        featureCollection = FeatureCollection.fromFeatures(features);
        poiSource = new GeoJsonSource.Builder(POI_SOURCE_ID)
                .featureCollection(featureCollection)
                .build();
        style.addSource(poiSource);

        FillLayer selectedLayer = new FillLayer(POI_SELECTED_LAYER_ID, POI_SOURCE_ID)
                .fillColor("rgba(61, 220, 132, 0.5)")
                .fillOpacity(
                        Expression.match(
                                Expression.get(PROPERTY_SELECTED),
                                Expression.literal(true),
                                Expression.literal(0.7),
                                Expression.literal(0.0)
                        )
                );
        style.addLayer(selectedLayer);
    }

    private JsonObject createPoiProperties(String name, String description) {
        JsonObject properties = new JsonObject();
        properties.addProperty(PROPERTY_NAME, name);
        properties.addProperty(PROPERTY_DESC, description);
        properties.addProperty(PROPERTY_SELECTED, false);
        return properties;
    }

    private final OnMapClickListener clickListener = point -> {
        mapboxMap.queryRenderedFeatures(
                mapboxMap.pixelForCoordinate(point),
                options -> {
                    if (options != null && !options.isEmpty()) {
                        Feature clickedFeature = options.get(0).getFeature();
                        String featureName = clickedFeature.getStringProperty(PROPERTY_NAME);
                        if (featureName != null) {
                            selectFeature(featureName);
                            infoTitle.setText(featureName);
                            infoDescription.setText(clickedFeature.getStringProperty(PROPERTY_DESC));
                            infoCard.setVisibility(View.VISIBLE);
                        }
                    } else {
                        selectFeature(null);
                        infoCard.setVisibility(View.GONE);
                    }
                    return false;
                },
                POI_SELECTED_LAYER_ID
        );
        return true;
    };

    private void setupClickListener() {
        GesturesUtils.getGestures(mapView).addOnMapClickListener(clickListener);
    }

    private void selectFeature(String featureName) {
        if (featureCollection != null && featureCollection.features() != null) {
            List<Feature> updatedFeatures = new ArrayList<>();
            for (Feature feature : featureCollection.features()) {
                String currentName = feature.getStringProperty(PROPERTY_NAME);
                feature.addBooleanProperty(PROPERTY_SELECTED, currentName != null && currentName.equals(featureName));
                updatedFeatures.add(feature);
            }
            featureCollection = FeatureCollection.fromFeatures(updatedFeatures);
            poiSource.featureCollection(featureCollection);
        }
    }

    // Métodos de ciclo de vida do MapView
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GesturesUtils.getGestures(mapView).removeOnMapClickListener(clickListener);
        mapView.onDestroy();
    }
}