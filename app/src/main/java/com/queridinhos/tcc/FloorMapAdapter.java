// app/src/main/java/com/queridinhos/tcc/FloorMapAdapter.java
package com.queridinhos.tcc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class FloorMapAdapter extends RecyclerView.Adapter<FloorMapAdapter.FloorMapViewHolder> {

    private final List<FloorMap> floorMaps;

    public FloorMapAdapter(List<FloorMap> floorMaps) {
        this.floorMaps = floorMaps;
    }

    @NonNull
    @Override
    public FloorMapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_floor_map, parent, false);
        return new FloorMapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorMapViewHolder holder, int position) {
        FloorMap floorMap = floorMaps.get(position);
        holder.bind(floorMap);
    }

    @Override
    public int getItemCount() {
        return floorMaps.size();
    }

    static class FloorMapViewHolder extends RecyclerView.ViewHolder {
        private final PhotoView floorMapImageView;

        public FloorMapViewHolder(@NonNull View itemView) {
            super(itemView);
            floorMapImageView = itemView.findViewById(R.id.floorMapImageView);
        }

        public void bind(FloorMap floorMap) {
            // --- AJUSTE PARA IMAGENS GRANDES ---
            // Usamos um método otimizado para carregar a imagem
            floorMapImageView.setImageBitmap(
                    decodeSampledBitmapFromResource(
                            itemView.getResources(),
                            floorMap.getImageResId(),
                            1024, // Largura máxima desejada
                            1024  // Altura máxima desejada
                    )
            );
            floorMapImageView.setMaximumScale(6.0f);
        }

        /**
         * Calcula o fator de subamostragem para a imagem, economizando memória.
         */
        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        /**
         * Decodifica a imagem de forma eficiente para evitar o erro de "Bitmap too large".
         */
        public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
            // Primeiro, decodifica com inJustDecodeBounds=true para checar as dimensões
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calcula o inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decodifica o bitmap com o inSampleSize setado
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }
    }
}