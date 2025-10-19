// Substitua o conteúdo de app/src/main/java/com/queridinhos/tcc/HighlightView.java
package com.queridinhos.tcc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class HighlightView extends View {

    private final Paint paint = new Paint();
    private Path originalRoomPath;
    private final Path transformedRoomPath = new Path();
    private final Matrix drawMatrix = new Matrix();
    private ValueAnimator animator;

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    // Recebe a matriz já calculada
    public void setDrawMatrix(Matrix matrix) {
        this.drawMatrix.set(matrix);
        invalidate();
    }

    public void highlight(Path path, Runnable onAnimationEnd) {
        this.originalRoomPath = new Path(path);

        if (animator != null && animator.isRunning()) animator.cancel();

        animator = ValueAnimator.ofArgb(Color.TRANSPARENT, Color.argb(150, 255, 215, 0), Color.TRANSPARENT);
        animator.setDuration(1000);
        animator.setRepeatCount(5);
        animator.addUpdateListener(animation -> {
            paint.setColor((Integer) animation.getAnimatedValue());
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                paint.setColor(Color.argb(120, 255, 215, 0));
                invalidate();
                if (onAnimationEnd != null) onAnimationEnd.run();
            }
        });
        animator.start();
    }

    public void clear() {
        if (animator != null && animator.isRunning()) animator.cancel();
        this.originalRoomPath = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (originalRoomPath != null && !originalRoomPath.isEmpty()) {
            originalRoomPath.transform(drawMatrix, transformedRoomPath);
            canvas.drawPath(transformedRoomPath, paint);
        }
    }
}