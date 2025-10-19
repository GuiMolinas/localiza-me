// Substitua o conteúdo de app/src/main/java/com/queridinhos/tcc/FloorMapAdapter.java
package com.queridinhos.tcc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
        private final Matrix manualMatrix = new Matrix();

        public FloorMapViewHolder(@NonNull View itemView) {
            super(itemView);
            floorMapImageView = itemView.findViewById(R.id.floorMapImageView);
            highlightView = itemView.findViewById(R.id.highlightView);
        }

        public void bind(FloorMap floorMap, OnMapTapListener listener) {
            Bitmap bitmap = decodeSampledBitmapFromResource(
                    itemView.getResources(),
                    floorMap.getImageResId(),
                    2048,
                    2048
            );
            floorMapImageView.setImageBitmap(bitmap);

            // Ação para recalcular a matriz manualmente
            final Runnable updateManualMatrix = () -> {
                if (bitmap == null || floorMapImageView.getWidth() == 0) return;

                final int viewWidth = floorMapImageView.getWidth();
                final int viewHeight = floorMapImageView.getHeight();
                final int imageWidth = bitmap.getWidth();
                final int imageHeight = bitmap.getHeight();

                manualMatrix.reset();

                // Calcula a escala para caber na tela (lógica do fitCenter)
                float scaleX = (float) viewWidth / imageWidth;
                float scaleY = (float) viewHeight / imageHeight;
                float scale = Math.min(scaleX, scaleY); // Usa a menor escala para caber tudo

                // Calcula o espaço extra para centralizar
                float dx = (viewWidth - imageWidth * scale) / 2f;
                float dy = (viewHeight - imageHeight * scale) / 2f;

                // Aplica a transformação
                manualMatrix.postScale(scale, scale);
                manualMatrix.postTranslate(dx, dy);

                highlightView.setDrawMatrix(manualMatrix);
            };

            // Espera o layout ficar pronto para fazer o cálculo inicial
            floorMapImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateManualMatrix.run();
                    floorMapImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

            // Listener para o Modo Desenvolvedor (agora usa a matriz manual para mais precisão)
            floorMapImageView.setOnViewTapListener((view, x, y) -> {
                if (listener != null) {
                    float[] touchPoint = {x, y};
                    Matrix inverseMatrix = new Matrix();
                    manualMatrix.invert(inverseMatrix); // Usa nossa matriz manual
                    inverseMatrix.mapPoints(touchPoint);
                    listener.onMapTap(touchPoint[0], touchPoint[1]);
                }
            });
        }

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