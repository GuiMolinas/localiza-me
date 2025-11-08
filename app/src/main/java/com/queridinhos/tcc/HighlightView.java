// app/src/main/java/com/queridinhos/tcc/HighlightView.java
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

    // Matriz 1: A matriz de zoom/pan do PhotoView
    private final Matrix drawMatrix = new Matrix();

    // Matriz 2: A matriz para escalar o Path original para o tamanho do Bitmap
    private final Matrix preScaleMatrix = new Matrix();

    // Dimensões para calcular a Matriz 2
    private int originalImageWidth = 0;
    private int originalImageHeight = 0;
    private int bitmapWidth = 0;
    private int bitmapHeight = 0;

    private ValueAnimator animator;
    private boolean isTestMode = false;

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    /**
     * Recebe a matriz de zoom/pan ATUAL da PhotoView.
     */
    public void setDrawMatrix(Matrix matrix) {
        this.drawMatrix.set(matrix);
        invalidate(); // Redesenha com a nova matriz de zoom
    }

    /**
     * Novo método: Recebe as dimensões para calcular a escala.
     */
    public void setImageInfo(int originalWidth, int originalHeight, int bmpWidth, int bmpHeight) {
        this.originalImageWidth = originalWidth;
        this.originalImageHeight = originalHeight;
        this.bitmapWidth = bmpWidth;
        this.bitmapHeight = bmpHeight;

        // Calcula a matriz de pré-dimensionamento
        if (originalImageWidth > 0 && originalImageHeight > 0) {
            float scaleX = (float) bitmapWidth / originalImageWidth;
            float scaleY = (float) bitmapHeight / originalImageHeight;
            this.preScaleMatrix.setScale(scaleX, scaleY);
        }
    }

    // NOVO MÉTODO DE TESTE (seu código original)
    public void drawTestMarkers(int imageWidth, int imageHeight) {
        isTestMode = true;
        Path testPath = new Path();
        float markerRadius = 50f;
        testPath.addCircle(0, 0, markerRadius, Path.Direction.CW);
        testPath.addCircle(imageWidth, 0, markerRadius, Path.Direction.CW);
        testPath.addCircle(0, imageHeight, markerRadius, Path.Direction.CW);
        testPath.addCircle(imageWidth, imageHeight, markerRadius, Path.Direction.CW);
        this.originalRoomPath = testPath;
        // Precisamos definir as dimensões para o onDraw funcionar
        setImageInfo(imageWidth, imageHeight, imageWidth, imageHeight);
        invalidate();
    }


    public void highlight(Path path, Runnable onAnimationEnd) {
        isTestMode = false;
        this.originalRoomPath = new Path(path);

        if (animator != null && animator.isRunning()) animator.cancel();

        animator = ValueAnimator.ofArgb(Color.TRANSPARENT, Color.argb(150, 255, 215, 0), Color.TRANSPARENT);
        animator.setDuration(1000);
        animator.setRepeatCount(5); // Seu código original
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
        // Se não tivermos o caminho ou as dimensões, não desenhe
        if (originalRoomPath == null || originalRoomPath.isEmpty() || bitmapWidth == 0) {
            return;
        }

        if (isTestMode) {
            paint.setColor(Color.argb(200, 255, 0, 255));
        }

        // ===== ESTA É A CORREÇÃO =====
        // 1. Aplica o pré-dimensionamento (Converte de 4000px para 1024px)
        originalRoomPath.transform(preScaleMatrix, transformedRoomPath);

        // 2. Aplica a matriz de zoom/pan do PhotoView (Converte de 1024px para a tela)
        transformedRoomPath.transform(drawMatrix, transformedRoomPath);

        // 3. Desenha o caminho final na tela
        canvas.drawPath(transformedRoomPath, paint);
    }
}