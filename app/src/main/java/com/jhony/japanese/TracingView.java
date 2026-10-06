package com.jhony.japanese;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TracingView extends View {
    public interface OnStrokeEvaluatedListener { void onEvaluationComplete(int score, boolean passed); }
    private String kana = "あ";
    private int kanaIndex = 0;
    private OnStrokeEvaluatedListener listener;
    private final Paint guide = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path strokePath = new Path();
    private final ArrayList<Path> userPaths = new ArrayList<Path>();
    private final List<List<P>> rawStrokes = new ArrayList<List<P>>();
    private List<P> curPts;
    private Path curPath;
    private float lastX, lastY;
    private boolean playing = false;
    private int playStroke = -1, playPoint = 0;
    public static class P { public float x, y; public P(float x, float y){this.x=x;this.y=y;} }
    private static P[][][] STROKES;

    public TracingView(Context c) { super(c); init(c); }
    public TracingView(Context c, AttributeSet a) { super(c, a); init(c); }
    public void setOnStrokeEvaluatedListener(OnStrokeEvaluatedListener l) { this.listener = l; }

    private void init(Context c) {
        if (STROKES == null) {
            try {
                InputStream is = c.getAssets().open("strokes.bin");
                DataInputStream dis = new DataInputStream(is);
                short nk = dis.readShort();
                STROKES = new P[nk][][];
                for (int k = 0; k < nk; k++) {
                    short ns = dis.readShort();
                    STROKES[k] = new P[ns][];
                    for (int s = 0; s < ns; s++) {
                        short np = dis.readShort();
                        STROKES[k][s] = new P[np];
                        for (int p = 0; p < np; p++) STROKES[k][s][p] = new P(dis.readFloat(), dis.readFloat());
                    }
                }
                dis.close();
            } catch (Exception e) { STROKES = new P[0][][]; }
        }
        guide.setTextAlign(Paint.Align.CENTER);
        guide.setColor(Color.rgb(225,225,225));
        grid.setStyle(Paint.Style.STROKE); grid.setColor(Color.rgb(235,235,235));
        ink.setStyle(Paint.Style.STROKE); ink.setStrokeWidth(16); ink.setStrokeCap(Paint.Cap.ROUND); ink.setColor(Color.rgb(33,33,33));
        guidePaint.setStyle(Paint.Style.STROKE); guidePaint.setStrokeWidth(6); guidePaint.setColor(Color.rgb(180,180,180));
    }

    public void setKana(String k, int idx) { kana = k; kanaIndex = idx; clearWriting(); playing = false; invalidate(); }
    public void clearWriting() { userPaths.clear(); rawStrokes.clear(); curPath = null; invalidate(); }
    public void playStrokes() { clearWriting(); playing = true; playStroke = 0; playPoint = 0; invalidate(); }

    private float gx(float nx) { return getWidth()*0.5f + (nx-0.5f)*Math.min(getWidth(), getHeight())*0.7f; }
    private float gy(float ny) { return getHeight()*0.5f + (ny-0.5f)*Math.min(getWidth(), getHeight())*0.7f; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float size = Math.min(getWidth(), getHeight()) * 0.82f;
        float l = (getWidth()-size)/2f, t = (getHeight()-size)/2f;
        c.drawRect(l, t, l+size, t+size, grid);
        c.drawLine(l+size/2f, t, l+size/2f, t+size, grid);
        c.drawLine(l, t+size/2f, l+size, t+size/2f, grid);
        guide.setTextSize(size * 0.68f);
        c.drawText(kana, getWidth()/2f, t+size*0.72f, guide);

        if (STROKES != null && STROKES.length > 0) {
            P[][] strokes = STROKES[Math.min(kanaIndex, STROKES.length - 1)];
            for (int s = 0; s < strokes.length; s++) {
                if (playing && s > playStroke) continue;
                strokePath.reset();
                int limit = (playing && s == playStroke) ? Math.min(playPoint + 1, strokes[s].length) : strokes[s].length;
                for (int j = 0; j < limit; j++) {
                    float px = gx(strokes[s][j].x), py = gy(strokes[s][j].y);
                    if (j == 0) strokePath.moveTo(px, py); else strokePath.lineTo(px, py);
                }
                c.drawPath(strokePath, guidePaint);
            }
            if (playing && playStroke < strokes.length) {
                playPoint++;
                if (playPoint >= strokes[playStroke].length) { playStroke++; playPoint = 0; if (playStroke >= strokes.length) playing = false; }
                postInvalidateDelayed(90);
            }
        }
        for (Path p : userPaths) c.drawPath(p, ink);
        if (curPath != null) c.drawPath(curPath, ink);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                curPath = new Path(); curPath.moveTo(x, y);
                lastX = x; lastY = y;
                curPts = new ArrayList<P>(); curPts.add(new P(x, y));
                invalidate(); return true;
            case MotionEvent.ACTION_MOVE:
                if (curPath != null) {
                    curPath.quadTo(lastX, lastY, (lastX+x)/2f, (lastY+y)/2f);
                    lastX = x; lastY = y;
                    curPts.add(new P(x, y));
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (curPath != null) {
                    curPath.lineTo(x, y);
                    curPts.add(new P(x, y));
                    userPaths.add(curPath);
                    rawStrokes.add(curPts);
                    curPath = null;
                    invalidate();
                    eval();
                }
                return true;
        }
        return super.onTouchEvent(e);
    }

    private void eval() {
        if (STROKES == null || STROKES.length == 0 || listener == null) return;
        P[][] exp = STROKES[Math.min(kanaIndex, STROKES.length - 1)];
        if (rawStrokes.size() >= exp.length) {
            float total = 0f, tol = Math.min(getWidth(), getHeight()) * 0.12f;
            for (int i = 0; i < exp.length && i < rawStrokes.size(); i++) {
                P[] es = exp[i]; List<P> as = rawStrokes.get(i);
                float sumDist = 0f;
                for (P ep : es) {
                    float mind = Float.MAX_VALUE;
                    for (P ap : as) mind = Math.min(mind, (float)Math.hypot(gx(ep.x)-ap.x, gy(ep.y)-ap.y));
                    sumDist += mind;
                }
                total += Math.max(0f, 1f - (sumDist/es.length)/tol) * 100f;
            }
            int score = Math.max(0, Math.min(100, Math.round(total / exp.length)));
            listener.onEvaluationComplete(score, score >= 65);
        }
    }
}