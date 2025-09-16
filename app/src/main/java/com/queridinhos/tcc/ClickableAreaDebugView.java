package com.queridinhos.tcc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ClickableAreaDebugView extends View {

    private final Paint paint = new Paint();
    private final List<MapActivity.ClickableArea> clickableAreas = new ArrayList<>();
    private final List<Integer> areaColors = new ArrayList<>();
    private Matrix matrix = new Matrix();

    public ClickableAreaDebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setClickableAreas(List<MapActivity.ClickableArea> areas) {
        clickableAreas.clear();
        areaColors.clear();
        int[] colors = {
                Color.argb(100, 255, 0, 0),      // Vermelho
                Color.argb(100, 0, 255, 0),      // Verde
                Color.argb(100, 0, 0, 255),      // Azul
                Color.argb(100, 255, 255, 0),    // Amarelo
                Color.argb(100, 0, 255, 255),    // Ciano
                Color.argb(100, 255, 0, 255),    // Magenta
                Color.argb(100, 128, 0, 128),    // Roxo
                Color.argb(100, 255, 165, 0),    // Laranja
                Color.argb(100, 0, 128, 0)       // Verde Escuro
        };
        int colorIndex = 0;
        for (MapActivity.ClickableArea area : areas) {
            clickableAreas.add(area);
            areaColors.add(colors[colorIndex % colors.length]);
            colorIndex++;
        }
        invalidate();
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (clickableAreas.isEmpty()) {
            return;
        }
        canvas.save();
        canvas.concat(matrix);
        for (int i = 0; i < clickableAreas.size(); i++) {
            paint.setColor(areaColors.get(i));
            canvas.drawPath(clickableAreas.get(i).getPath(), paint); // Alterado para drawPath
        }
        canvas.restore();
    }
}