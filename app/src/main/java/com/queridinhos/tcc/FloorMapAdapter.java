// app/src/main/java/com/queridinhos/tcc/FloorMapAdapter.java
package com.queridinhos.tcc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Remova o ViewTreeObserver, não é mais necessário
// import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;
import android.util.SparseArray;

public class FloorMapAdapter extends RecyclerView.Adapter<FloorMapAdapter.FloorMapViewHolder> {

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
        holder.bind(floorMap, mapTapListener);
        highlightViews.put(position, holder.highlightView);
        if (holder.highlightView != null) {
            holder.highlightView.clear();
        }
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

        // Não precisamos mais da matriz manual
        // private final Matrix manualMatrix = new Matrix();

        public FloorMapViewHolder(@NonNull View itemView) {
            super(itemView);
            floorMapImageView = itemView.findViewById(R.id.floorMapImageView);
            highlightView = itemView.findViewById(R.id.highlightView);
        }

        public void bind(FloorMap floorMap, OnMapTapListener listener) {
            Resources res = itemView.getResources();
            int resId = floorMap.getImageResId();

            // ===== ETAPA 1: LER AS DIMENSÕES ORIGINAIS DA IMAGEM =====
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
            final int originalImageWidth = options.outWidth;
            final int originalImageHeight = options.outHeight;

            // ===== ETAPA 2: CARREGAR A IMAGEM (POTENCIALMENTE REDUZIDA) =====
            // (Seu código original, está correto)
            Bitmap bitmap = decodeSampledBitmapFromResource(res, resId, 2048, 2048);
            floorMapImageView.setImageBitmap(bitmap);

            // ===== ETAPA 3: PEGAR AS DIMENSÕES DO BITMAP REDUZIDO =====
            final int bitmapWidth = bitmap.getWidth();
            final int bitmapHeight = bitmap.getHeight();

            // ===== ETAPA 4: INFORMAR O HIGHLIGHTVIEW =====
            // Esta é a nova linha crucial.
            // Ela informa à view de destaque as dimensões originais E as novas.
            highlightView.setImageInfo(originalImageWidth, originalImageHeight, bitmapWidth, bitmapHeight);

            // Não precisamos mais do cálculo manual da matriz
            // O OnMatrixChangeListener fará todo o trabalho.
            /* final Runnable updateManualMatrix = () -> { ... };
            floorMapImageView.getViewTreeObserver().addOnGlobalLayoutListener(...);
            */

            // Listener para o Modo Desenvolvedor
            floorMapImageView.setOnViewTapListener((view, x, y) -> {
                if (listener != null) {
                    float[] touchPoint = {x, y};
                    Matrix inverseMatrix = new Matrix();
                    floorMapImageView.getImageMatrix().invert(inverseMatrix);
                    inverseMatrix.mapPoints(touchPoint);
                    // IMPORTANTE: As coordenadas de toque agora são relativas
                    // ao bitmap redimensionado (ex: 1024x768), não ao original.
                    listener.onMapTap(touchPoint[0], touchPoint[1]);
                }
            });

            // Se o usuário der zoom, passamos a matriz para o HighlightView
            // (Seu código original, está correto e é essencial)
            floorMapImageView.setOnMatrixChangeListener(rect -> {
                if (highlightView != null) {
                    highlightView.setDrawMatrix(floorMapImageView.getImageMatrix());
                }
            });
        }


        // (Seus métodos decodeSampledBitmapFromResource e calculateInSampleSize permanecem iguais)
        public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }
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
    }
}