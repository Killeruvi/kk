package com.CrimsonVeilDev.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class ParticleView extends View {
    private static final int PARTICLE_COUNT = 45; // Balance between density and performance
    private Particle[] particles;
    private Paint paint;
    private Random random;

    public ParticleView(Context context, AttributeSet
attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        random = new Random();
        particles = new Particle[PARTICLE_COUNT];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw,
int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Initialize particles across the entire screen layout height
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = createParticle(w, h, true);
        }
    }

    private Particle createParticle(int width, int
height, boolean randomY) {
        Particle p = new Particle();
        p.x = random.nextFloat() * width;
        // Start them scattered initially; later spawns always appear from the bottom
        p.y = randomY ? (random.nextFloat() * height) :
(height + 20);
        p.radius = 3 + random.nextFloat() * 6;      //Size variance (3dp to 9dp)
        p.speedY = 1.2f + random.nextFloat() * 2.5f; //Floating upward speed
        p.speedX = -0.5f + random.nextFloat() * 1.0f; //Soft side-to-side sway
        p.alpha = 40 + random.nextInt(160);         //Random glow opacity
        return p;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Particle p = particles[i];
            if (p == null) continue;

            // Apply frame step physics changes
            p.y -= p.speedY;
            p.x += p.speedX;

            // Gradually fade out particles as theyapproach the top ceiling
            if (p.y < height * 0.3f) {
                p.alpha -= 2;
            }

            // Respawn clean particle at the bottom ifit drifts off screen or dies
            if (p.y < 0 || p.alpha <= 0 || p.x < 0 ||
p.x > width) {
                particles[i] = createParticle(width,
height, false);
                continue;
            }

            // Render glowing blood-red/crimson embernode
            paint.setColor(Color.argb(p.alpha, 255, 42,
42));
            canvas.drawCircle(p.x, p.y, p.radius,
paint);
        }

        // Force next-frame draw loop at 60 FPS natively
        postInvalidateOnAnimation();
    }

    private static class Particle {
        float x, y;
        float radius;
        float speedY, speedX;
        int alpha;
    }
}