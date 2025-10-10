// src/main/java/com/queridinhos/tcc/InternalMapActivity.java
package com.queridinhos.tcc;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.HashMap;
import java.util.Map;

public class InternalMapActivity extends AppCompatActivity {

    private PhotoView internalMapImageView;
    private ImageButton backButton;

    // Mapeamento dos nomes dos blocos para os recursos de imagem
    private final Map<String, Integer> blockMapImages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_map);

        internalMapImageView = findViewById(R.id.internalMapImageView);
        backButton = findViewById(R.id.backButtonInternal);

        // Preenche o mapa com as imagens internas (você precisará adicionar essas imagens)
        initializeBlockMaps();

        String blockName = getIntent().getStringExtra("BLOCK_NAME");

        if (blockName != null && blockMapImages.containsKey(blockName)) {
            internalMapImageView.setImageResource(blockMapImages.get(blockName));
        } else {
            Toast.makeText(this, "Mapa interno não disponível para este bloco.", Toast.LENGTH_LONG).show();
            // Opcional: mostrar uma imagem padrão de "não encontrado"
            // internalMapImageView.setImageResource(R.drawable.mapa_nao_encontrado);
        }

        backButton.setOnClickListener(v -> finish());
    }

    private void initializeBlockMaps() {
        blockMapImages.put("Bloco A", R.drawable.mapa_interno_bloco_a1);
    }
}