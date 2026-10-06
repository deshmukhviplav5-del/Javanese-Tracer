package com.jhony.japanese;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String[] HIRAGANA = {
        "あ", "い", "う", "え", "お",
        "か", "き", "く", "け", "こ",
        "さ", "し", "す", "せ", "そ",
        "た", "ち", "つ", "て", "と",
        "な", "に", "ぬ", "ね", "の",
        "は", "ひ", "ふ", "へ", "ほ",
        "ま", "み", "む", "め", "も",
        "や", "ゆ", "よ",
        "ら", "り", "る", "れ", "ろ",
        "わ", "を", "ん"
    };

    private static final String[] KATAKANA = {
        "ア", "イ", "ウ", "エ", "オ",
        "カ", "キ", "ク", "ケ", "コ",
        "サ", "シ", "ス", "セ", "ソ",
        "タ", "チ", "ツ", "テ", "ト",
        "ナ", "ニ", "ヌ", "ネ", "ノ",
        "ハ", "ヒ", "フ", "ヘ", "ホ",
        "マ", "ミ", "ム", "メ", "モ",
        "ヤ", "ユ", "ヨ",
        "ラ", "リ", "ル", "レ", "ロ",
        "ワ", "ヲ", "ン"
    };

    private static final String[] ROMAJI = {
        "a", "i", "u", "e", "o",
        "ka", "ki", "ku", "ke", "ko",
        "sa", "shi", "su", "se", "so",
        "ta", "chi", "tsu", "te", "to",
        "na", "ni", "nu", "ne", "no",
        "ha", "hi", "fu", "he", "ho",
        "ma", "mi", "mu", "me", "mo",
        "ya", "yu", "yo",
        "ra", "ri", "ru", "re", "ro",
        "wa", "wo", "n"
    };

    private boolean isKatakana = false;
    private int currentIndex = 0;

    private TracingView tracingView;
    private TextView tvKanaTitle;
    private TextView tvScoreBadge;
    private Button btnModeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tracingView = (TracingView) findViewById(R.id.tracingView);
        tvKanaTitle = (TextView) findViewById(R.id.tvKanaTitle);
        tvScoreBadge = (TextView) findViewById(R.id.tvScoreBadge);
        btnModeToggle = (Button) findViewById(R.id.btnModeToggle);

        Button btnPrev = (Button) findViewById(R.id.btnPrev);
        Button btnNext = (Button) findViewById(R.id.btnNext);
        Button btnReplay = (Button) findViewById(R.id.btnReplay);
        Button btnClear = (Button) findViewById(R.id.btnClear);

        tracingView.setOnStrokeEvaluatedListener(new TracingView.OnStrokeEvaluatedListener() {
            @Override
            public void onEvaluationComplete(final int score, final boolean passed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScoreBadge.setText(String.format("Score: %d%%", score));
                        if (passed) {
                            tvScoreBadge.setBackgroundColor(Color.parseColor("#C8E6C9"));
                            tvScoreBadge.setTextColor(Color.parseColor("#1B5E20"));
                            Toast.makeText(MainActivity.this, "Great job! (合格)", Toast.LENGTH_SHORT).show();
                        } else {
                            tvScoreBadge.setBackgroundColor(Color.parseColor("#FFCDD2"));
                            tvScoreBadge.setTextColor(Color.parseColor("#B71C1C"));
                        }
                    }
                });
            }
        });

        btnModeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isKatakana = !isKatakana;
                btnModeToggle.setText(isKatakana ? "Katakana" : "Hiragana");
                updateView();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentIndex > 0) {
                    currentIndex--;
                    updateView();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentIndex < HIRAGANA.length - 1) {
                    currentIndex++;
                    updateView();
                }
            }
        });

        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracingView.playStrokes();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracingView.clearWriting();
                resetScoreBadge();
            }
        });

        updateView();
    }

    private void updateView() {
        String currentKana = isKatakana ? KATAKANA[currentIndex] : HIRAGANA[currentIndex];
        String romaji = ROMAJI[currentIndex];
        
        // Hiragana indexes: 0..45, Katakana indexes: 46..91 in strokes.bin
        int assetIndex = isKatakana ? (46 + currentIndex) : currentIndex;

        tvKanaTitle.setText(String.format("%s (%s)", currentKana, romaji));
        resetScoreBadge();
        tracingView.setKana(currentKana, assetIndex);
    }

    private void resetScoreBadge() {
        tvScoreBadge.setText("Score: --");
        tvScoreBadge.setBackgroundColor(Color.parseColor("#E0E0E0"));
        tvScoreBadge.setTextColor(Color.parseColor("#424242"));
    }
}
