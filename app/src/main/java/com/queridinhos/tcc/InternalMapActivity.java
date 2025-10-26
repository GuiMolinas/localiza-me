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
// Salas do Bloco G em ordem alfabética
        blocoGRooms.add(new Room("Lab. de Ergonomia e Acústica", 0, createPath(832f, 98f, 1540f, 102f, 1540f, 1017f, 835f, 1017f)));
        blocoGRooms.add(new Room("Lab. de Maquetes", 0, createPath(1805f, 1302f, 3248f, 1306f, 3249f, 2219f, 1805f, 2221f)));
        blocoGRooms.add(new Room("Portaria", 0, createPath(102f, 2502f, 695f, 2503f, 697f, 2817f, 102f, 2818f)));
        blocoGRooms.add(new Room("Pós Graduação", 0, createPath(3264f, 1447f, 4067f, 1447f, 4219f, 1623f, 4220f, 2225f, 3266f, 2221f)));
        blocoGRooms.add(new Room("Sala 001", 0, createPath(832f, 1304f, 1528f, 1305f, 1531f, 2220f, 835f, 2219f)));
        blocoGRooms.add(new Room("Sala 002", 0, createPath(109f, 1303f, 817f, 1305f, 816f, 2221f, 107f, 2222f)));
        blocoGRooms.add(new Room("Sala 005", 0, createPath(1560f, 99f, 2269f, 100f, 2268f, 1017f, 1562f, 1016f)));
        blocoGRooms.add(new Room("Sala 103", 0, createPath(105f, 96f, 816f, 94f, 814f, 1016f, 110f, 1019f)));
        blocoGRooms.add(new Room("Sala 104", 1, createPath(876f, 115f, 1580f, 116f, 1580f, 1032f, 874f, 1034f)));
        blocoGRooms.add(new Room("Sala 105", 1, createPath(1601f, 113f, 2305f, 115f, 2306f, 1033f, 1597f, 1035f)));
        blocoGRooms.add(new Room("Sala 106", 1, createPath(3306f, 1323f, 3427f, 1269f, 3615f, 1466f, 4102f, 1465f, 4258f, 1641f, 4255f, 2244f, 3305f, 2236f)));
        blocoGRooms.add(new Room("Sala 107", 1, createPath(2577f, 1324f, 3289f, 1316f, 3286f, 2237f, 2580f, 2236f)));
        blocoGRooms.add(new Room("Sala 108", 1, createPath(1840f, 1318f, 2560f, 1321f, 2560f, 2241f, 1842f, 2237f)));
        blocoGRooms.add(new Room("Sala 201", 2, createPath(899f, 1342f, 1596f, 1342f, 1592f, 2260f, 898f, 2257f)));
        blocoGRooms.add(new Room("Sala 202", 2, createPath(172f, 1339f, 877f, 1339f, 877f, 2260f, 171f, 2262f)));
        blocoGRooms.add(new Room("Sala 203", 2, createPath(172f, 135f, 880f, 134f, 881f, 1051f, 174f, 1051f)));
        blocoGRooms.add(new Room("Sala 204", 2, createPath(898f, 135f, 1603f, 136f, 1605f, 1053f, 897f, 1057f)));
        blocoGRooms.add(new Room("Sala 205", 2, createPath(1625, 137f, 2333f, 135f, 2331f, 1053f, 1625f, 1052f)));
        blocoGRooms.add(new Room("Sala 206", 2, createPath(3330f, 1343f, 3482f, 1338f, 3640f, 1486f, 4125f, 1486f, 4278f, 1666f, 4238f, 2258f, 3332f, 2258f)));
        blocoGRooms.add(new Room("Sala 208", 2, createPath(1868, 1339f, 2585f, 1341f, 2584f, 2260f, 1866f, 2254f)));
        blocoGRooms.add(new Room("Sala 301", 3, createPath(813f, 1328f, 1507f, 1326f, 1507f, 2242f, 813f, 2243f)));
        blocoGRooms.add(new Room("Sala 302", 3, createPath(88f, 1326f, 794f, 1325f, 794f, 2246f, 81f, 2256f)));
        blocoGRooms.add(new Room("Sala 303", 3, createPath(86f, 120f, 793f, 122f, 797f, 1038f, 89f, 1039f)));
        blocoGRooms.add(new Room("Sala 304", 3, createPath(813f, 124f, 1519f, 119f, 1519f, 1041f, 816f, 1035f)));
        blocoGRooms.add(new Room("Sala 305", 3, createPath(1541f, 122f, 2248f, 123f, 2242f, 1041f, 1543f, 1036f)));
        blocoGRooms.add(new Room("Sala 306", 3, createPath(3247f, 1329f, 3354f, 1237f, 3560f, 1474f, 4046f, 1474f, 4196f, 1651f, 4194f, 2245f, 3247f, 2241f)));
        blocoGRooms.add(new Room("Sala 307", 3, createPath(2518f, 1362f, 3228f, 1330f, 3227f, 2243f, 2521f, 2243f)));
        blocoGRooms.add(new Room("Sala 308", 3, createPath(1782f, 1324f, 2502f, 1328f, 2500f, 2244f, 1784f, 2244f)));
        blocoGRooms.add(new Room("Sala 401", 4, createPath(132f, 1286f, 1548f, 1284f, 1548f, 2201f, 134f, 2203f)));
        blocoGRooms.add(new Room("Sala 402", 4, createPath(128f, 76f, 1078f, 80f, 1086f, 1254f, 130f, 1256f)));
        blocoGRooms.add(new Room("Sala 403", 4, createPath(1107f, 80f, 2285f, 82f, 2287f, 994f, 1107f, 998f)));
        blocoGRooms.add(new Room("Sala 404", 4, createPath(3290, 1286f, 3397f, 1197f, 3597f, 1432f, 4088f, 1432f, 4236f, 1607f, 4238f, 2202f, 3290f, 2197f)));
        blocoGRooms.add(new Room("Sala 405", 4, createPath(1817f, 1281f, 3269f, 1285f, 3268f, 2201f, 1827f, 2199f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 01", 1, createPath(872f, 1320f, 1385f, 1318f, 1385f, 1611f, 874f, 1611f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 02", 1, createPath(872f, 1632f, 1385f, 1633f, 1385f, 1924f, 875f, 1924f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 03", 1, createPath(874f, 1942f, 1568f, 1946f, 1568f, 2237f, 873f, 2235f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 04", 1, createPath(148f, 1315f, 672f, 1319f, 672f, 1613f, 147f, 1611f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 05", 1, createPath(147f, 1633f, 671f, 1633f, 667f, 1923f, 147f, 1926f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 06", 1, createPath(147f, 1942f, 856f, 1946f, 849f, 2238f, 147f, 2239f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 07", 1, createPath(149f, 738f, 670f, 742f, 670f, 1033f, 145f, 1034f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 08", 1, createPath(147f, 427f, 669f, 426f, 669f, 721f, 145f, 720f)));
        blocoGRooms.add(new Room("Sala de Atendimento/Supervisão 09", 1, createPath(145f, 112f, 849f, 115f, 847f, 426f, 148f, 407f)));
        blocoGRooms.add(new Room("San. Feminino (1° Pavimento)", 1, createPath(3160f, 752f, 3500f, 752f, 4085f, 1442f, 3630f, 1444f, 3289f, 1046f, 3292f, 891f, 3159f, 894f)));
        blocoGRooms.add(new Room("San. Feminino (2° Pavimento)", 2, createPath(3180f, 774f, 3522f, 773f, 4111f, 1463f, 3650f, 1460f, 3317f, 1066f, 3182f, 1049f)));
        blocoGRooms.add(new Room("San. Feminino (3° Pavimento)", 3, createPath(3101f, 757f, 3441f, 760f, 4024f, 1449f, 3573f, 1448f, 3235f, 1054f, 3239f, 899f, 3100f, 896f)));
        blocoGRooms.add(new Room("San. Feminino (4° Pavimento)", 4, createPath(3140, 720f, 3478f, 715f, 4068f, 1403f, 3615f, 1403f, 3275f, 1008f, 3273f, 855f, 3141f, 858f)));
        blocoGRooms.add(new Room("San. Feminino (Térreo)", 0, createPath(3119f, 736f, 3462f, 737f, 4049f, 1427f, 3591f, 1428f, 3254f, 1032f, 3252f, 876f, 3121f, 877f)));
        blocoGRooms.add(new Room("San. Masculino (1° Pavimento)", 1, createPath(2646f, 287f, 3039f, 749f, 3139f, 753f, 3139f, 892f, 3003f, 893f, 3004f, 1035f, 2645f, 1031f)));
        blocoGRooms.add(new Room("San. Masculino (2° Pavimento)", 2, createPath(2668f, 303f, 3062f, 771f, 3161f, 773f, 3163f, 911f, 3164f, 1051f, 2671f, 1053f)));
        blocoGRooms.add(new Room("San. Masculino (3° Pavimento)", 3, createPath(2585f, 294f, 2978f, 758f, 3082f, 761f, 3079f, 898f, 2945f, 899f, 2943f, 1038f, 2585f, 1036f)));
        blocoGRooms.add(new Room("San. Masculino (4° Pavimento)", 4, createPath(2623f, 253f, 3021f, 717f, 3117f, 717f, 3119f, 851f, 2985f, 861f, 2983f, 995f, 2631f, 995f)));
        blocoGRooms.add(new Room("San. Masculino (Térreo)", 0, createPath(2607f, 267f, 2998f, 733f, 3099f, 735f, 3102f, 877f, 2966f, 876f, 2964f, 1018f, 2605f, 1017f)));
        blockRoomsMap.put("Bloco G", blocoGRooms);

        List<Room> blocoDRooms = new ArrayList<>();
        blocoDRooms.add(new Room("Atendimento", 1, createPath(254f, 3337f, 261f, 3727f, 1084f, 3723f, 1091f, 3896f, 1426f, 3901f, 1431f, 3661f, 1509f, 3658f, 1511f, 3436f, 1098f, 3433f, 1098f, 3348f)));
        blocoDRooms.add(new Room("Avaliação de Saúde Ocular", 2, createPath(2131f, 1007f, 3409f, 1009f, 3475f, 1485f, 2125f, 1486f)));
        blocoDRooms.add(new Room("Bromatologia", 1, createPath(1539f, 159f, 1836f, 149f, 1838f, 1295f, 1539f, 1297f)));
        blocoDRooms.add(new Room("Clínica Nutrição", 1, createPath(894f, 175f, 1509f, 160f, 1508f, 1296f, 872f, 1296f)));
        blocoDRooms.add(new Room("Coleta", 0, createPath(7058f, 3200f, 7947f, 3209f, 8056f, 3907f, 7054f, 3904f)));
        blocoDRooms.add(new Room("Consultorio Farmaceutico", 1, createPath(2578f, 1809f, 2993f, 1812f, 2995f, 2065f, 2583f, 2065f)));
        blocoDRooms.add(new Room("Coordenação", 0, createPath(4280f, 3181f, 4650f, 3186f, 4633f, 3898f, 4267f, 3899f)));
        blocoDRooms.add(new Room("Coordenação Curso de Enfer. ED. Fisica, Gestão Hosp., Hist.", 0, createPath(3709f, 2001f, 4276f, 1998f, 4246f, 3088f, 3715f, 3082f)));
        blocoDRooms.add(new Room("Coordenação de Cursos", 1, createPath(1525f, 3439f, 2519f, 3444f, 2519f, 3177f, 3763f, 3178f, 3864f, 3901f, 1524f, 3888f)));
        blocoDRooms.add(new Room("Depósito de Reagentes", 1, createPath(2128f, 2638f, 2426f, 2638f, 2424f, 3119f, 2128f, 3121f)));
        blocoDRooms.add(new Room("Dispensação", 1, createPath(2129f, 2295f, 2556f, 2298f, 2554f, 2693f, 2451f, 2693f, 2451f, 2610f, 2131f, 2610f)));
        blocoDRooms.add(new Room("Gabinete - Sala Escura", 2, createPath(1868f, 162f, 3267f, 121f, 3315f, 477f, 1867f, 477f)));
        blocoDRooms.add(new Room("Gabinete Professor Tempo Integral", 0, createPath(3226f, 1996f, 3687f, 1996f, 3682f, 3075f, 3228f, 3078f)));
        blocoDRooms.add(new Room("Lab. Controle de Qualidade", 1, createPath(3018f, 1810f, 3568f, 1809f, 3661f, 2453f, 3416f, 2452f, 3418f, 2347f, 3199f, 2347f, 3199f, 2228f, 3024f, 2230f)));
        blocoDRooms.add(new Room("Lab. de Eng. Mecanica 01, Processos de Produção Mecanica e Robotica, Manufatura Integrada por Computador", 0, createPath(5313f, 1578f, 7761f, 1596f, 7953f, 3108f, 5270f, 3089f, 5276f, 2853f, 5043f, 2852f, 5070f, 1894f, 5306f, 1893f)));
        blocoDRooms.add(new Room("Lab. de Eng. Mecanica 02, Circuitos Fluidos Mecanicos, Sistemas Sequenciais e Controle, Metalografia, Mat. de Construção Mecanica e Metrologia", 0, createPath(5834f, 129f, 7563f, 84f, 7756f, 1570f, 5313f, 1551f, 5313f, 1144f, 5839f, 1143f)));
        blocoDRooms.add(new Room("Lab. de Informática 01", 0, createPath(142f, 1911f, 3196f, 1910f, 3198f, 3077f, 102f, 3004f)));
        blocoDRooms.add(new Room("Lab. de Informática 02", 0, createPath(197f, 206f, 2323f, 191f, 2323f, 1342f, 160f, 1345f)));
        blocoDRooms.add(new Room("Lab. de Informática 03", 0, createPath(2719f, 193f, 4328f, 159f, 4295f, 1342f, 2722f, 1344f)));
        blocoDRooms.add(new Room("Lab. Estudos sobre Desenvolvimento Motor Humano", 0, createPath(4661f, 3419f, 5383f, 3421f, 5378f, 3895f, 4655f, 3896f)));
        blocoDRooms.add(new Room("Lab. Fisiologia", 0, createPath(5404f, 3192f, 7037f, 3205f, 7033f, 3415f, 6674f, 3412f, 6672f, 3901f, 5400f, 3899f)));
        blocoDRooms.add(new Room("Lab. Semi Solidos e Liquidos", 1, createPath(2450f, 2718f, 3218f, 2711f, 3219f, 3122f, 2451f, 3123f)));
        blocoDRooms.add(new Room("Lab. Solidos", 1, createPath(3416f, 2478f, 3663f, 2481f, 3753f, 3119f, 3246f, 3119f, 3248f, 2718f, 3403f, 2719f)));
        blocoDRooms.add(new Room("Laboratório Multidisciplinar de Alimentos", 1, createPath(1866f, 148f, 3345f, 94f, 3568f, 1776f, 2136f, 1780f, 2138f, 1395f, 1926f, 1390f, 1868f, 1303f)));
        blocoDRooms.add(new Room("Reabilitação Visual - Sala Escura", 2, createPath(2126f, 501f, 3320f, 503f, 3382f, 981f, 2125f, 982f)));
        blocoDRooms.add(new Room("Recepção", 1, createPath(871f, 1326f, 1509f, 1326f, 1506f, 1601f, 867f, 1614f)));
        blocoDRooms.add(new Room("Recepção Farmacia Escola", 1, createPath(2151f, 1807f, 2553f, 1805f, 2553f, 2264f, 2126f, 2264f)));
        blocoDRooms.add(new Room("Sala de Aula 201", 2, createPath(2126f, 1514f, 3478f, 1514f, 3697f, 3142f, 2126f, 3140f)));
        blocoDRooms.add(new Room("Sala de Aula 203", 2, createPath(846f, 197f, 1841f, 166f, 1845f, 1618f, 815f, 1637f)));
        blocoDRooms.add(new Room("Sala de Aula 204", 2, createPath(806f, 1983f, 2102f, 1987f, 2101f, 3141f, 783f, 3165f)));
        blocoDRooms.add(new Room("Sala Professor", 0, createPath(6693f, 3431f, 7033f, 3436f, 7032f, 3904f, 6692f, 3901f)));
        blocoDRooms.add(new Room("Sanitario Feminino (1° Pavimento)", 1, createPath(1954f, 3177f, 2499f, 3182f, 2496f, 3425f, 1946f, 3422f)));
        blocoDRooms.add(new Room("Sanitario Feminino (Térreo)", 0, createPath(5118f, 146f, 5810f, 131f, 5818f, 621f, 5106f, 615f)));
        blocoDRooms.add(new Room("Sanitario Masculino (1° Pavimento)", 1, createPath(1531f, 3180f, 1926f, 3180f, 1925f, 3420f, 1528f, 3417f)));
        blocoDRooms.add(new Room("Sanitario Masculino (Térreo)", 0, createPath(5295f, 639f, 5817f, 644f, 5821f, 1126f, 5282f, 1122f)));
        blocoDRooms.add(new Room("Vestiário/Paramnetação", 1, createPath(2578f, 2090f, 2997f, 2090f, 2995f, 2407f, 2578f, 2409f)));
        blockRoomsMap.put("Bloco D", blocoDRooms);

        List<Room> blocoERooms = new ArrayList<>();
        blocoERooms.add(new Room("Lab. Enfermagem 01 - Lab. de Suporte à Vida", 2, createPath(1514f, 2490f, 3538f, 2510f, 3523f, 3958f, 245f, 3923f, 195f, 3870f)));
        blocoERooms.add(new Room("Lab. Enfermagem 02", 2, createPath(5623f, 982f, 8127f, 1012f, 6999f, 4018f, 5068f, 3978f, 5088f, 2585f, 5622f, 2573f)));
        blocoERooms.add(new Room("Lab. Multidisciplinar 01 - Fisioterapia", 1, createPath(5628f, 979f, 8137f, 977f, 7002f, 4021f, 5070f, 3968f, 5092f, 2566f, 5625f, 2571f)));
        blocoERooms.add(new Room("Lab. Multidisciplinar 02 - Fisioterapia", 1, createPath(2974f, 994f, 4378f, 998f, 4383f, 2492f, 3513f, 2430f, 3520f, 3940f, 210f, 3905f)));
        blocoERooms.add(new Room("Multidisciplinar 03 - Fisioterapia", 2, createPath(2938f, 988f, 4379f, 992f, 4379f, 2488f, 1541f, 2465f)));
        blocoERooms.add(new Room("San. Acessivel (1° Pavimento)", 1, createPath(4120f, 2972f, 4538f, 2975f, 4533f, 3475f, 4110f, 3465f)));
        blocoERooms.add(new Room("San. Acessivel (2° Pavimento)", 2, createPath(4114f, 2961f, 4539f, 2973f, 4529f, 3461f, 4106f, 3458f)));
        blocoERooms.add(new Room("San. Fem. (1° Pavimento)", 1, createPath(4575f, 2982f, 5045f, 2990f, 5023f, 3987f, 4275f, 3972f, 4285f, 3512f, 4565f, 3505f)));
        blocoERooms.add(new Room("San. Fem. (2° Pavimento)", 2, createPath(4574f, 2968f, 5046f, 2986f, 5029f, 3978f, 4279f, 3968f, 4286f, 3501f, 4569f, 3501f)));
        blocoERooms.add(new Room("San. Masculino (1° Pavimento)", 1, createPath(3573f, 2957f, 4075f, 2970f, 4065f, 3500f, 4223f, 3512f, 4213f, 3970f, 3560f, 3962f)));
        blocoERooms.add(new Room("San. Masculino (2° Pavimento)", 2, createPath(3569f, 2953f, 4076f, 2966f, 4069f, 3488f, 4219f, 3498f, 4211f, 3966f, 3559f, 3953f)));
        blockRoomsMap.put("Bloco E", blocoERooms);

        List<Room> blocoCRooms = new ArrayList<>();
        blocoCRooms.add(new Room("Avaliação Sistematizada", 0, createPath(1349f, 2432f, 2092f, 2437f, 2092f, 2960f, 1352f, 2958f)));
        blocoCRooms.add(new Room("Coordenação", 0, createPath(4045f, 1254f, 4657f, 1262f, 4665f, 1820f, 4043f, 1814f)));
        blocoCRooms.add(new Room("Direção", 0, createPath(4039f, 1847f, 4665f, 1847f, 4663f, 2761f, 4047f, 2765f)));
        blocoCRooms.add(new Room("Lab. Análises do Movimento", 0, createPath(6236f, 3476f, 8236f, 3478f, 8239f, 4660f, 6226f, 4660f)));
        blocoCRooms.add(new Room("Pediatria", 0, createPath(4690f, 3117f, 5685f, 3122f, 5685f, 3467f, 6180f, 3477f, 6200f, 3510f, 6197f, 4656f, 4693f, 4653f)));
        blocoCRooms.add(new Room("Recepção", 0, createPath(3178f, 599f, 5684f, 599f, 5680f, 1229f, 3159f, 1227f, 3161f, 623f, 3185f, 615f)));
        blocoCRooms.add(new Room("Sala de Aula 101", 1, createPath(602f, 83f, 2089f, 85f, 2101f, 86f, 2102f, 1525f, 590f, 1527f, 591f, 85f)));
        blocoCRooms.add(new Room("Sala de Aula 102", 1, createPath(592f, 2042f, 2101f, 2043f, 2103f, 3478f, 588f, 3476f)));
        blocoCRooms.add(new Room("Sala de Aula 103", 1, createPath(2130f, 83f, 2866f, 81f, 2869f, 1523f, 2129f, 1523f)));
        blocoCRooms.add(new Room("Sala de Aula 104", 1, createPath(2894f, 83f, 3639f, 84f, 3634f, 1523f, 2894f, 1523f)));
        blocoCRooms.add(new Room("Sala de Aula 105", 1, createPath(2124f, 2045f, 3635f, 2043f, 3635f, 3477f, 2130f, 3487f)));
        blocoCRooms.add(new Room("Sala de Aula 106/108", 1, createPath(4658f, 2045f, 6164f, 2043f, 6158f, 3480f, 4663f, 3478f)));
        blocoCRooms.add(new Room("Sala de Aula 107/109", 1, createPath(4655f, 79f, 6161f, 81f, 6165f, 1523f, 4657f, 1523f)));
        blocoCRooms.add(new Room("Sala de Aula 110", 1, createPath(6199f, 2298f, 8198f, 2298f, 8203f, 3484f, 6193f, 3484f)));
        blocoCRooms.add(new Room("Sala de Aula 111", 1, createPath(6187f, 78f, 8207f, 79f, 8196f, 1261f, 6193f, 1252f)));
        blocoCRooms.add(new Room("Sala de Aula 201", 2, createPath(621f, 143f, 2131f, 142f, 2131f, 1584f, 622f, 1584f)));
        blocoCRooms.add(new Room("Sala de Aula 202", 2, createPath(622f, 2102f, 2131f, 2098f, 2131f, 3537f, 621f, 3536f)));
        blocoCRooms.add(new Room("Sala de Aula 203", 2, createPath(2169f, 138f, 3657f, 138f, 3667f, 1584f, 2158f, 1584f)));
        blocoCRooms.add(new Room("Sala de Aula 204", 2, createPath(2156f, 2101f, 3665f, 2101f, 3659f, 3540f, 2164f, 3539f)));
        blocoCRooms.add(new Room("Sala de Aula 205", 2, createPath(4691f, 136f, 6185f, 136f, 6197f, 1582f, 4685f, 1583f)));
        blocoCRooms.add(new Room("Sala de Aula 206", 2, createPath(4690f, 2100f, 6193f, 2098f, 6191f, 3543f, 4688f, 3543f)));
        blocoCRooms.add(new Room("Sala de Aula 207", 2, createPath(6223f, 133f, 8239f, 131f, 8224f, 1338f, 6227f, 1313f)));
        blocoCRooms.add(new Room("Sala de Aula 208", 2, createPath(6227f, 2354f, 8234f, 2356f, 8241f, 3545f, 6224f, 3542f)));
        blocoCRooms.add(new Room("Sala de Aula 301", 3, createPath(621f, 110f, 2119f, 112f, 2120f, 1561f, 611f, 1557f)));
        blocoCRooms.add(new Room("Sala de Aula 302", 3, createPath(612f, 2078f, 2123f, 2074f, 2113f, 3517f, 606f, 3517f)));
        blocoCRooms.add(new Room("Sala de Aula 303", 3, createPath(2162f, 112f, 3648f, 118f, 3656f, 1557f, 2148f, 1555f)));
        blocoCRooms.add(new Room("Sala de Aula 304", 3, createPath(2151f, 2077f, 3654f, 2075f, 3653f, 3517f, 2146f, 3517f)));
        blocoCRooms.add(new Room("Sala de Aula 305", 3, createPath(4681f, 110f, 6179f, 110f, 6189f, 1555f, 4673f, 1555f)));
        blocoCRooms.add(new Room("Sala de Aula 306", 3, createPath(4675f, 2072f, 6185f, 2076f, 6179f, 3516f, 4677f, 3516f)));
        blocoCRooms.add(new Room("Sala de Aula 307", 3, createPath(6213f, 116f, 8224f, 110f, 8222f, 1282f, 6220f, 1282f)));
        blocoCRooms.add(new Room("Sala de Aula 308", 3, createPath(6217f, 2340f, 8224f, 2330f, 8228f, 3519f, 6212f, 3517f)));
        blocoCRooms.add(new Room("Sala dos Alunos", 0, createPath(3301f, 4075f, 3665f, 4073f, 3669f, 4651f, 3159f, 4653f, 3155f, 4299f, 3303f, 4293f)));
        blocoCRooms.add(new Room("Sala dos Professores", 0, createPath(1040f, 2983f, 2088f, 2986f, 2093f, 3483f, 1040f, 3479f)));
        blocoCRooms.add(new Room("San. Feminino (Térreo)", 0, createPath(628f, 2984f, 1016f, 2982f, 1018f, 3480f, 654f, 3480f, 656f, 3442f, 626f, 3442f)));
        blocoCRooms.add(new Room("San. Masc. Professores (Térreo)", 0, createPath(655f, 2436f, 1019f, 2437, 1015f, 2957f, 627f, 2956f, 625f, 2474f, 654f, 2474f, 654f, 2476f)));
        blocoCRooms.add(new Room("Sanit. Acessivel Feminino (Térreo)", 0, createPath(5709f, 600f, 6203f, 602f, 6207f, 902f, 5709f, 900f)));
        blocoCRooms.add(new Room("Sanit. Acessivel Masculino (Térreo)", 0, createPath(5708f, 928f, 6208f, 926f, 6212f, 1174f, 6186f, 1182f, 6182f, 1230f, 5712f, 1228f)));
        blocoCRooms.add(new Room("Sanitário Fem. (1° Pavimento)", 1, createPath(3663f, 81f, 4130f, 81f, 4131f, 1331f, 3660f, 1332f)));
        blocoCRooms.add(new Room("Sanitário Masc. (1° Pavimento)", 1, createPath(4158f, 1334f, 4160f, 78f, 4625f, 84f, 4633f, 1332f)));
        blocoCRooms.add(new Room("Vest. Fem (2° Pavimento)", 2, createPath(3700f, 138f, 4159f, 139f, 4162f, 1388f, 3695f, 1392f)));
        blocoCRooms.add(new Room("Vest. Fem.", 0, createPath(8242f, 1259f, 8242f, 1837f, 7967f, 1837f, 7967f, 1252f)));
        blocoCRooms.add(new Room("Vest. Fem. (3° Pavimento)", 3, createPath(3689f, 112f, 4155f, 114f, 4155f, 1363f, 3685f, 1363f)));
        blocoCRooms.add(new Room("Vest. Masc.", 0, createPath(7965f, 1858f, 8240f, 1866f, 8240f, 2401f, 8212f, 2441f, 7975f, 2441f)));
        blocoCRooms.add(new Room("Vest. Masc. (2° Pavimento)", 2, createPath(4189f, 141f, 4650f, 139f, 4659f, 1394f, 4192f, 1392f)));
        blocoCRooms.add(new Room("Vest. Masc. (3° Pavimento)", 3, createPath(4181f, 106f, 4649f, 118f, 4649f, 1366f, 4181f, 1368f)));
        blocoCRooms.add(new Room("Vestiário Feminino", 0, createPath(5715f, 2476f, 6652f, 2478f, 6660f, 2921f, 5905f, 2921f, 5905f, 2921f, 5910f, 2756f, 5713f, 2751f)));
        blocoCRooms.add(new Room("Vestiário Masculino", 0, createPath(5910f, 2961f, 6657f, 2966f, 6655f, 3441f, 5710f, 3441f, 5715f, 3136f, 5908f, 3128f)));
        blockRoomsMap.put("Bloco C", blocoCRooms);


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