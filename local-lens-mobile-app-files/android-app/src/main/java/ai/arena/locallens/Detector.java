package ai.arena.locallens;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class Detector {
    public static final float THRESHOLD = 0.65f;
    private OrtEnvironment env;
    private OrtSession session;

    public static class Result {
        public final String label;
        public final boolean isAI;
        public final float confidence;
        public final float aiProbability;
        public final long inferenceMs;

        public Result(String label, boolean isAI, float confidence, float aiProbability, long inferenceMs) {
            this.label = label;
            this.isAI = isAI;
            this.confidence = confidence;
            this.aiProbability = aiProbability;
            this.inferenceMs = inferenceMs;
        }
    }

    public Detector(Context context) {
        try {
            env = OrtEnvironment.getEnvironment();
            InputStream is = context.getAssets().open("nonescape-mini-v0-fp16-storage.onnx");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            session = env.createSession(buffer, new OrtSession.SessionOptions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Result analyze(Bitmap bitmap) {
        long t0 = System.currentTimeMillis();
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        FloatBuffer buffer = FloatBuffer.allocate(3 * 224 * 224);

        int[] pixels = new int[224 * 224];
        scaled.getPixels(pixels, 0, 224, 0, 0, 224, 224);

        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};

        for (int c = 0; c < 3; c++) {
            for (int i = 0; i < pixels.length; i++) {
                int p = pixels[i];
                float val;
                if (c == 0) val = ((p >> 16) & 0xFF) / 255.0f;
                else if (c == 1) val = ((p >> 8) & 0xFF) / 255.0f;
                else val = (p & 0xFF) / 255.0f;
                buffer.put(((val - mean[c]) / std[c]));
            }
        }
        buffer.rewind();

        try {
            OnnxTensor tensor = OnnxTensor.createTensor(env, buffer, new long[]{1, 3, 224, 224});
            OrtSession.Result result = session.run(Collections.singletonMap("x", tensor));
            float[][] probs = (float[][]) result.get(0).getValue();
            float rawAi = probs[0][1];
            float aiProb = calibratedAiProbability(rawAi);
            boolean isAI = aiProb >= THRESHOLD;
            float conf = isAI ? aiProb : (1.0f - aiProb);
            long inferMs = System.currentTimeMillis() - t0;
            return new Result(isAI ? "AI" : "REAL", isAI, conf, aiProb, inferMs);
        } catch (Exception e) {
            return new Result("ERROR", false, 0f, 0f, 0);
        }
    }

    public static float calibratedAiProbability(float raw) {
        float bounded = Math.min(1.0f - 1e-6f, Math.max(1e-6f, raw));
        float rawLogit = (float) Math.log(bounded / (1.0f - bounded));
        float offset = (float) (Math.log(0.65 / (1.0 - 0.65)) - Math.log(0.5 / (1.0 - 0.5)));
        return (float) (1.0 / (1.0 + Math.exp(-(rawLogit + offset))));
    }
}
