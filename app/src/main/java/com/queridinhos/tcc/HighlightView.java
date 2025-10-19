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
    private boolean isTestMode = false; // Flag para modo de teste

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setDrawMatrix(Matrix matrix) {
        this.drawMatrix.set(matrix);
        invalidate();
    }

    // NOVO MÉTODO DE TESTE
    public void drawTestMarkers(int imageWidth, int imageHeight) {
        isTestMode = true;
        Path testPath = new Path();
        // Círculos com raio de 50 pixels na imagem original
        float markerRadius = 50f;
        testPath.addCircle(0, 0, markerRadius, Path.Direction.CW); // Canto Superior Esquerdo
        testPath.addCircle(imageWidth, 0, markerRadius, Path.Direction.CW); // Canto Superior Direito
        testPath.addCircle(0, imageHeight, markerRadius, Path.Direction.CW); // Canto Inferior Esquerdo
        testPath.addCircle(imageWidth, imageHeight, markerRadius, Path.Direction.CW); // Canto Inferior Direito
        this.originalRoomPath = testPath;
        invalidate();
    }


    public void highlight(Path path, Runnable onAnimationEnd) {
        isTestMode = false;
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
        isTestMode = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (originalRoomPath == null || originalRoomPath.isEmpty()) return;

        // Se estiver no modo de teste, usa uma cor diferente para ficar óbvio
        if (isTestMode) {
            paint.setColor(Color.argb(200, 255, 0, 255)); // Magenta
        }

        originalRoomPath.transform(drawMatrix, transformedRoomPath);
        canvas.drawPath(transformedRoomPath, paint);
    }
}