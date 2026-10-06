package com.jhony.japanese;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String[] KANA = {"あ", "い", "う", "え", "お"};
    private static final String[] ROMAJI = {"a", "i", "u", "e", "o"};
    private int cur = 0;
    private TracingView tv;
    private TextView tvTitle, tvScore;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        tv = (TracingView) findViewById(R.id.tracingView);
        tvTitle = (TextView) findViewById(R.id.tvKanaTitle);
        tvScore = (TextView) findViewById(R.id.tvScoreBadge);
        tv.setOnStrokeEvaluatedListener(new TracingView.OnStrokeEvaluatedListener() {
            @Override public void onEvaluationComplete(final int score, final boolean pass) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        tvScore.setText("Score: " + score + "%");
                        tvScore.setBackgroundColor(pass ? Color.parseColor("#C8E6C9") : Color.parseColor("#FFCDD2"));
                    }
                });
            }
        });
        findViewById(R.id.btnPrev).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (cur > 0) { cur--; sync(); } }
        });
        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (cur < KANA.length - 1) { cur++; sync(); } }
        });
        findViewById(R.id.btnReplay).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { tv.playStrokes(); }
        });
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { tv.clearWriting(); tvScore.setText("Score: --"); }
        });
        sync();
    }
    private void sync() {
        tvTitle.setText(KANA[cur] + " (" + ROMAJI[cur] + ")");
        tvScore.setText("Score: --");
        tv.setKana(KANA[cur], cur);
    }
}