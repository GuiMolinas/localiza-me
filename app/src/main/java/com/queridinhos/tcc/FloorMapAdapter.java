// Substitua todo o conteúdo de app/src/main/java/com/queridinhos/tcc/FloorMapAdapter.java por este código

package com.queridinhos.tcc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class FloorMapAdapter extends RecyclerView.Adapter<FloorMapAdapter.FloorMapViewHolder> {

    // Interface para comunicar os toques no mapa para a Activity
    public interface OnMapTapListener {
        void onMapTap(float imageX, float imageY);
    }

    private final List<FloorMap> floorMaps;
    private final OnMapTapListener mapTapListener;
    private final SparseArray<HighlightView> highlightViews = new SparseArray<>();

    public FloorMapAdapter(List<FloorMap> floorMaps, OnMapTapListener listener) {
        this.floorMaps = floorMaps;
        this.mapTapListener = listener;
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
        holder.bind(floorMap, mapTapListener); // Passa o listener para o ViewHolder
        highlightViews.put(position, holder.highlightView);
        holder.highlightView.clear();
    }

    @Override
    public int getItemCount() {
        return floorMaps.size();
    }

    public HighlightView getHighlightViewForPosition(int position) {
        return highlightViews.get(position);
    }

    static class FloorMapViewHolder extends RecyclerView.ViewHolder {
        private final PhotoView floorMapImageView;
        private final HighlightView highlightView;

        public FloorMapViewHolder(@NonNull View itemView) {
            super(itemView);
            floorMapImageView = itemView.findViewById(R.id.floorMapImageView);
            highlightView = itemView.findViewById(R.id.highlightView);
        }

        public void bind(FloorMap floorMap, OnMapTapListener listener) {
            floorMapImageView.setImageBitmap(
                    decodeSampledBitmapFromResource(
                            itemView.getResources(),
                            floorMap.getImageResId(),
                            2048, // Aumentar a qualidade da imagem base
                            2048
                    )
            );
            floorMapImageView.setMaximumScale(6.0f);

            // Sincroniza a matriz do PhotoView (zoom/pan) com a HighlightView
            floorMapImageView.setOnMatrixChangeListener(rect -> {
                highlightView.setMatrix(floorMapImageView.getImageMatrix());
            });

            // Listener de toque para o "Modo Desenvolvedor"
            floorMapImageView.setOnViewTapListener((view, x, y) -> {
                if (listener != null) {
                    // ESTA É A CORREÇÃO IMPORTANTE
                    // Converte as coordenadas do toque na TELA para coordenadas da IMAGEM ORIGINAL
                    float[] touchPoint = { x, y };
                    Matrix inverseMatrix = new Matrix();
                    floorMapImageView.getImageMatrix().invert(inverseMatrix);
                    inverseMatrix.mapPoints(touchPoint);
                    listener.onMapTap(touchPoint[0], touchPoint[1]);
                }
            });
        }

        // --- Métodos para otimização de Bitmap (sem alterações) ---
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

        public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }
    }
}