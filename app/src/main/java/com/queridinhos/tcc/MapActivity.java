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

        List<Point> coordinates = Arrays.asList(
                Point.fromLngLat(0, mapHeight),
                Point.fromLngLat(mapWidth, mapHeight),
                Point.fromLngLat(mapWidth, 0),
                Point.fromLngLat(0, 0)
        );

        ImageSource imageSource = new ImageSource.Builder(IMAGE_SOURCE_ID)
                .coordinates(coordinates)
                .build();
        style.addSource(imageSource);

        RasterLayer rasterLayer = new RasterLayer("map-image-layer", IMAGE_SOURCE_ID);
        style.addLayer(rasterLayer);

        Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapa);

        ImageSource sourceFromStyle = (ImageSource) style.getSource(IMAGE_SOURCE_ID);
        if (sourceFromStyle != null) {
            sourceFromStyle.updateImage(mapBitmap);
        }
    }


    private void setupPOIs(Style style) {
        List<Feature> features = new ArrayList<>();

        // Bloco A - Exemplo Correto
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(669, 554), // Canto 1
                                Point.fromLngLat(713, 615), // Canto 2
                                Point.fromLngLat(943, 552), // Canto 3
                                Point.fromLngLat(899, 459), // Canto 4
                                Point.fromLngLat(669, 554)  // Canto 1 novamente para fechar
                        ))),
                createPoiProperties("Bloco A", "Prédio com salas de aula e laboratórios.")
        ));

        // TODO: Faça o mesmo para os outros blocos. Adicione os 4 (ou mais) cantos de cada um.
        // Biblioteca
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(765, 358),
                                Point.fromLngLat(800, 358),
                                Point.fromLngLat(800, 300),
                                Point.fromLngLat(765, 300),
                                Point.fromLngLat(765, 358)
                        ))),
                createPoiProperties("Biblioteca", "Acervo de livros, salas de estudo e computadores.")
        ));

        // Bloco B
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco B", "Descrição do Bloco B.")
        ));

        // Bloco C
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco C", "Descrição do Bloco C.")
        ));

        // Bloco D
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco D", "Descrição do Bloco D.")
        ));

        // Bloco E
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco E", "Descrição do Bloco E.")
        ));

        // Bloco F
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco F", "Descrição do Bloco F.")
        ));

        // Bloco G
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco G", "Descrição do Bloco G.")
        ));

        // Bloco Alfa
        features.add(Feature.fromGeometry(
                Polygon.fromLngLats(Collections.singletonList(
                        Arrays.asList(
                                Point.fromLngLat(934, 632),
                                Point.fromLngLat(1000, 632),
                                Point.fromLngLat(1000, 580),
                                Point.fromLngLat(934, 580),
                                Point.fromLngLat(934, 632)
                        ))),
                createPoiProperties("Bloco Alfa", "Descrição do Bloco Alfa.")
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
                    if (options.getValue() != null && !options.getValue().isEmpty()) {
                        Feature clickedFeature = options.getValue().get(0).getFeature();
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