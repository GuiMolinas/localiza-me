// Crie este novo arquivo: app/src/main/java/com/queridinhos/tcc/HighlightView.java
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
    private Path roomPath;
    private ValueAnimator animator;
    private Matrix matrix = new Matrix();

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setMatrix(Matrix matrix) {
        this.matrix.set(matrix);
        invalidate();
    }

    public void highlight(Path path, Runnable onAnimationEnd) {
        this.roomPath = new Path(path); // Copia o path para evitar modificação do original

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        // Animação para piscar 3 vezes (6 segundos no total)
        animator = ValueAnimator.ofArgb(Color.TRANSPARENT, Color.argb(150, 255, 215, 0), Color.TRANSPARENT);
        animator.setDuration(1000); // 1 segundo para acender e apagar
        animator.setRepeatCount(2); // Repete 5 vezes, totalizando 6 execuções
        animator.addUpdateListener(animation -> {
            paint.setColor((Integer) animation.getAnimatedValue());
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Ao final, deixa a sala com uma cor fixa
                paint.setColor(Color.argb(120, 255, 215, 0)); // Amarelo semi-transparente
                invalidate();
                if (onAnimationEnd != null) {
                    onAnimationEnd.run(); // Executa a ação de desbloquear a tela
                }
            }
        });
        animator.start();
    }

    public void clear() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        this.roomPath = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (roomPath != null) {
            canvas.save();
            canvas.concat(matrix);
            canvas.drawPath(roomPath, paint);
            canvas.restore();
        }
    }
}