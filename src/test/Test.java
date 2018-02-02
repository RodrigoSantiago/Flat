package test;

public class Test {
    static float stops[] = new float[]{0.01f, 0.1f, 0.2f, 0.3f, 1f};
    static int colors[] = new int[]{1, 100, 200, 100, 0};

    public static void main(String... args) {
        int color = 0;
        for (float j = 0; j <= 1; j += 0.01f) {
            int choose = stops.length - 1;
            for (int i = 0; i < stops.length; i++) {
                if (j < stops[i]) {
                    choose = i - 1;
                    break;
                }
            }
            if (choose == -1) color = colors[0];
            else if (choose == stops.length - 1) color = colors[stops.length - 1];
            else color = mix(colors[choose], colors[choose + 1], (j  - stops[choose]) / (stops[choose + 1] - stops[choose]));
            System.out.println( j + " : " + color);
        }
    }

    private static int mix(int colorA, int colorB, float t) {
        return (int) ((colorA * (1 - t)) + (colorB * t));
    }
}
