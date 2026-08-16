package ai.arena.locallens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.InputStream;

public class MainActivity extends Activity {
    private ImageView previewImage;
    private TextView resultLabel;
    private TextView resultDetails;
    private ProgressBar progressBar;
    private Detector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewImage = findViewById(R.id.preview_image);
        resultLabel = findViewById(R.id.result_label);
        resultDetails = findViewById(R.id.result_details);
        progressBar = findViewById(R.id.progress_bar);
        Button btnPick = findViewById(R.id.btn_pick);

        detector = new Detector(this);

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                processUri(imageUri);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            processUri(data.getData());
        }
    }

    private void processUri(Uri uri) {
        if (uri == null) return;
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap != null) {
                previewImage.setImageBitmap(bitmap);
                progressBar.setVisibility(View.VISIBLE);
                resultLabel.setText("Analyzing locally...");

                new Thread(() -> {
                    Detector.Result res = detector.analyze(bitmap);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        resultLabel.setText(res.label + " " + Math.round(res.confidence * 100) + "%");
                        resultLabel.setTextColor(res.isAI ? 0xFFEF5B3F : 0xFF10B981);
                        resultDetails.setText("AI Probability: " + String.format("%.2f", res.aiProbability * 100) + "%\nInference time: " + res.inferenceMs + " ms");
                    });
                }).start();
            }
        } catch (Exception e) {
            resultLabel.setText("Error reading image");
        }
    }
}
