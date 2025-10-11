// app/src/main/java/com/queridinhos/tcc/InternalMapActivity.java
package com.queridinhos.tcc;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalMapActivity extends AppCompatActivity {

    private ViewPager2 floorViewPager;
    private TextView floorNameTextView;
    private ImageButton backButton;

    // Estrutura de dados atualizada para suportar múltiplos andares por bloco
    private final Map<String, List<FloorMap>> blockFloorsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_map);

        floorViewPager = findViewById(R.id.floorViewPager);
        floorNameTextView = findViewById(R.id.floorNameTextView);
        backButton = findViewById(R.id.backButtonInternal);

        initializeBlockMaps();

        String blockName = getIntent().getStringExtra("BLOCK_NAME");
        List<FloorMap> floors = blockFloorsMap.get(blockName);

        if (floors != null && !floors.isEmpty()) {
            // Configura o ViewPager2 com o nosso adaptador
            FloorMapAdapter adapter = new FloorMapAdapter(floors);
            floorViewPager.setAdapter(adapter);

            // Atualiza o nome do andar quando o usuário arrasta a tela
            floorViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    floorNameTextView.setText(floors.get(position).getFloorName());
                }
            });

            // Define o nome do primeiro andar ao carregar a tela
            floorNameTextView.setText(floors.get(0).getFloorName());

        } else {
            Toast.makeText(this, "Mapas internos não disponíveis para este bloco.", Toast.LENGTH_LONG).show();
        }

        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Aqui você associa cada bloco a uma LISTA de andares.
     */
    private void initializeBlockMaps() {
        // Exemplo para o Bloco A, que tem Térreo e 1º Andar
        List<FloorMap> blocoAFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoAFloors.add(new FloorMap("Térreo", R.drawable.terreo_a));
        blocoAFloors.add(new FloorMap("Piso Superior", R.drawable.superior_a));
        blockFloorsMap.put("Bloco A", blocoAFloors);

        List<FloorMap> blocoBFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoBFloors.add(new FloorMap("Térreo", R.drawable.terreo_b));
        blockFloorsMap.put("Bloco B", blocoBFloors);

        List<FloorMap> blocoCFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoCFloors.add(new FloorMap("Térreo", R.drawable.terreo_c));
        blocoCFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_c));
        blocoCFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_c));
        blocoCFloors.add(new FloorMap("3º Pavimento", R.drawable.terceiro_c));
        blockFloorsMap.put("Bloco C", blocoCFloors);

        List<FloorMap> blocoDFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoDFloors.add(new FloorMap("Térreo", R.drawable.terreo_d));
        blocoDFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_d));
        blocoDFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_d));
        blockFloorsMap.put("Bloco D", blocoDFloors);

        List<FloorMap> blocoEFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoEFloors.add(new FloorMap("Térreo", R.drawable.terreo_e));
        blocoEFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_e));
        blocoEFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_e));
        blockFloorsMap.put("Bloco E", blocoEFloors);

        List<FloorMap> blocoFFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoFFloors.add(new FloorMap("Térreo", R.drawable.terreo_f));
        blocoFFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_f));
        blockFloorsMap.put("Bloco F", blocoFFloors);

        List<FloorMap> blocoGFloors = new ArrayList<>();
        // Adicione os arquivos de imagem na pasta res/drawable
        blocoGFloors.add(new FloorMap("Térreo", R.drawable.terreo_g));
        blocoGFloors.add(new FloorMap("1° Pavimento", R.drawable.primeiro_g));
        blocoGFloors.add(new FloorMap("2º Pavimento", R.drawable.segundo_g));
        blocoGFloors.add(new FloorMap("3º Pavimento", R.drawable.terceiro_g));
        blocoGFloors.add(new FloorMap("4º Pavimento", R.drawable.quarto_g));
        blockFloorsMap.put("Bloco G", blocoGFloors);




    }
}