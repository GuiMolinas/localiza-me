// app/src/main/java/com/queridinhos/tcc/FloorMapAdapter.java
package com.queridinhos.tcc;

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
            floorMapImageView.setImageResource(floorMap.getImageResId());
        }
    }
}