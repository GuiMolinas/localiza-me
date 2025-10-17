// Substitua todo o conteúdo de app/src/main/java/com/queridinhos/tcc/InternalMapActivity.java por este código

package com.queridinhos.tcc;

import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

// A CORREÇÃO ESTÁ AQUI: Adicionamos a implementação da interface
public class InternalMapActivity extends AppCompatActivity implements FloorMapAdapter.OnMapTapListener {

    private ViewPager2 floorViewPager;
    private TextView floorNameTextView;
    private Spinner roomSpinner;
    private ImageButton backButton;
    private FloorMapAdapter adapter;
    private TextView debugCoordinates; // Para o "Modo Desenvolvedor"

    private final Map<String, List<FloorMap>> blockFloorsMap = new HashMap<>();
    private final Map<String, List<Room>> blockRoomsMap = new HashMap<>();
    private List<Room> currentBlockRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_map);

        floorViewPager = findViewById(R.id.floorViewPager);
        floorNameTextView = findViewById(R.id.floorNameTextView);
        roomSpinner = findViewById(R.id.roomSpinner);
        backButton = findViewById(R.id.backButtonInternal);

        initializeBlockMaps();
        initializeRooms();

        String blockName = getIntent().getStringExtra("BLOCK_NAME");
        List<FloorMap> floors = blockFloorsMap.get(blockName);
        currentBlockRooms = blockRoomsMap.get(blockName);

        if (floors != null && !floors.isEmpty()) {
            // Agora 'this' é um OnMapTapListener válido, e o erro não ocorrerá
            adapter = new FloorMapAdapter(floors, this);
            floorViewPager.setAdapter(adapter);

            floorViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    floorNameTextView.setText(floors.get(position).getFloorName());
                }
            });

            floorNameTextView.setText(floors.get(0).getFloorName());
            setupRoomSpinner();
        } else {
            Toast.makeText(this, "Mapas internos não disponíveis para este bloco.", Toast.LENGTH_LONG).show();
        }

        backButton.setOnClickListener(v -> finish());
    }

    // ESTE MÉTODO É OBRIGATÓRIO POR CAUSA DA INTERFACE
    // É ele que recebe os toques do Adapter
    @Override
    public void onMapTap(float imageX, float imageY) {
        String coordsText = String.format(Locale.US, "X: %.1f, Y: %.1f", imageX, imageY);
        debugCoordinates.setText(coordsText);
        // O Toast é útil para ver rapidamente sem precisar olhar o canto da tela
        Toast.makeText(this, coordsText, Toast.LENGTH_SHORT).show();
    }

    private void setupRoomSpinner() {
        List<String> roomNames = new ArrayList<>();
        roomNames.add("Selecione uma sala...");

        if (currentBlockRooms != null) {
            roomNames.addAll(currentBlockRooms.stream().map(Room::getName).collect(Collectors.toList()));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roomNames);
        roomSpinner.setAdapter(spinnerAdapter);

        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearAllHighlights(); // Limpa destaques anteriores ao selecionar algo novo
                if (position > 0) {
                    String selectedRoomName = (String) parent.getItemAtPosition(position);
                    Room selectedRoom = findRoomByName(selectedRoomName);
                    if (selectedRoom != null) {
                        highlightRoom(selectedRoom);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void highlightRoom(Room room) {
        floorViewPager.setUserInputEnabled(false);
        floorViewPager.setCurrentItem(room.getFloorIndex(), true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            HighlightView highlightView = adapter.getHighlightViewForPosition(room.getFloorIndex());
            if (highlightView != null) {
                highlightView.highlight(room.getArea(), () -> {
                    floorViewPager.setUserInputEnabled(true);
                    Toast.makeText(this, "Você já pode explorar os andares.", Toast.LENGTH_SHORT).show();
                });
            } else {
                floorViewPager.setUserInputEnabled(true);
                Toast.makeText(this, "Erro: A view de destaque não foi encontrada.", Toast.LENGTH_SHORT).show();
            }
        }, 300);
    }

    private void clearAllHighlights() {
        if(adapter == null) return;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            HighlightView hv = adapter.getHighlightViewForPosition(i);
            if (hv != null) {
                hv.clear();
            }
        }
    }

    private Room findRoomByName(String name) {
        if (currentBlockRooms == null) return null;
        for (Room room : currentBlockRooms) {
            if (room.getName().equals(name)) {
                return room;
            }
        }
        return null;
    }

    private Path createPath(float... points) {
        Path path = new Path();
        if (points.length < 2) return path;
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            path.lineTo(points[i], points[i + 1]);
        }
        path.close();
        return path;
    }

    private void initializeRooms() {
        List<Room> blocoGRooms = new ArrayList<>();
        // Lembre-se de substituir estas coordenadas pelas que você pegar com o Modo Desenvolvedor
        blocoGRooms.add(new Room("Sala 103", 0, createPath(105f, 96f, 816f, 94f, 814f, 1016f, 110f, 1019f)));
        blocoGRooms.add(new Room("Lab. de Ergonomia e Acústica", 0, createPath(832f, 98f, 1540f, 102f, 1540f, 1017f, 835f, 1017f)));
        blocoGRooms.add(new Room("Sala 005", 0, createPath(1560f, 99f, 2269f, 100f, 2268f, 1017f, 1562f, 1016f)));
        blocoGRooms.add(new Room("San. Masculino (Térreo)", 0, createPath(2607f, 267f, 2998f, 733f, 3099f, 735f, 3102f, 877f, 2966f, 876f, 2964f, 1018f, 2605f, 1017f)));
        blocoGRooms.add(new Room("San. Feminino (Térreo)", 0, createPath(3119f, 736f, 3462f, 737f, 4049f, 1427f, 3591f, 1428f, 3254f, 1032f, 3252f, 876f, 3121f, 877f)));
        blocoGRooms.add(new Room("Pós Graduação", 0, createPath(3264f, 1447f, 4067f, 1447f, 4219f, 1623f, 4220f, 2225f, 3266f, 2221f)));
        blocoGRooms.add(new Room("Lab. de Maquetes", 0, createPath(1805f, 1302f, 3248f, 1306f, 3249f, 2219f, 1805f, 2221f)));
        blocoGRooms.add(new Room("Sala 001", 0, createPath(832f, 1304f, 1528f, 1305f, 1531f, 2220f, 835f, 2219f)));
        blocoGRooms.add(new Room("Sala 002", 0, createPath(109f, 1303f, 817f, 1305f, 816f, 2221f, 107f, 2222f)));
        blocoGRooms.add(new Room("Portaria", 0, createPath(102f, 2502f, 695f, 2503f, 697f, 2817f, 102f, 2818f)));

        // Adicione outras salas aqui...
        blockRoomsMap.put("Bloco G", blocoGRooms);
    }

    private void initializeBlockMaps() {
        // ... seu código para inicializar os mapas dos blocos (sem alterações) ...
        List<FloorMap> blocoAFloors = new ArrayList<>();
        blocoAFloors.add(new FloorMap("Térreo", R.drawable.inferior_a));
        blocoAFloors.add(new FloorMap("Piso Superior", R.drawable.superior_a));
        blockFloorsMap.put("Bloco A", blocoAFloors);

        List<FloorMap> blocoBFloors = new ArrayList<>();
        blocoBFloors.add(new FloorMap("Térreo", R.drawable.inferior_b));
        blocoBFloors.add(new FloorMap("Piso Superior", R.drawable.superior_b));
        blockFloorsMap.put("Bloco B", blocoBFloors);

        List<FloorMap> blocoCFloors = new ArrayList<>();
        blocoCFloors.add(new FloorMap("Térreo", R.drawable.terreo_c));
        blocoCFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_c));
        blocoCFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_c));
        blocoCFloors.add(new FloorMap("3º Pavimento", R.drawable.terceiro_c));
        blockFloorsMap.put("Bloco C", blocoCFloors);

        List<FloorMap> blocoDFloors = new ArrayList<>();
        blocoDFloors.add(new FloorMap("Térreo", R.drawable.terreo_d));
        blocoDFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_d));
        blocoDFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_d));
        blockFloorsMap.put("Bloco D", blocoDFloors);

        List<FloorMap> blocoEFloors = new ArrayList<>();
        blocoEFloors.add(new FloorMap("Térreo", R.drawable.terreo_e));
        blocoEFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_e));
        blocoEFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_e));
        blockFloorsMap.put("Bloco E", blocoEFloors);

        List<FloorMap> blocoFFloors = new ArrayList<>();
        blocoFFloors.add(new FloorMap("Térreo", R.drawable.terreo_f));
        blocoFFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_f));
        blockFloorsMap.put("Bloco F", blocoFFloors);

        List<FloorMap> blocoGFloors = new ArrayList<>();
        blocoGFloors.add(new FloorMap("Térreo", R.drawable.terreo_g));
        blocoGFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_g));
        blocoGFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_g));
        blocoGFloors.add(new FloorMap("3º Pavimento", R.drawable.terceiro_g));
        blocoGFloors.add(new FloorMap("4º Pavimento", R.drawable.quarto_g));
        blockFloorsMap.put("Bloco G", blocoGFloors);
    }
}