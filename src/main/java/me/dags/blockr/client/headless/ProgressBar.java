package me.dags.blockr.client.headless;

/**
 * @author dags <dags@dags.me>
 */
public class ProgressBar {

    private final int width;
    private final char[] buffer;

    public ProgressBar(int width) {
        this.width = width;
        this.buffer = new char[width + 6];
        this.buffer[0] = '[';
        this.buffer[1 + width] = ']';
        this.buffer[1 + width + 4] = '%';
    }

    public void appendTo(StringBuilder builder) {
        synchronized (buffer) {
            builder.append(buffer);
        }
    }

    public void setProgress(int value, int total) {
        synchronized (buffer) {
            float progress = value / (float) total;
            int percentage = Math.round(progress * 100);
            int width = Math.round(progress * this.width);

            for (int i = 1; i <= this.width; i++) {
                setBar(i, i > width ? ' ' : '=');
            }

            setPercentage(percentage);
        }
    }

    private void setBar(int pos, char c) {
        buffer[pos] = c;
    }

    private void setPercentage(int value) {
        if (value > 99) {
            buffer[1 + width + 1] = '1';
            buffer[1 + width + 2] = '0';
            buffer[1 + width + 3] = '0';
        } else if (value > 9) {
            buffer[1 + width + 1] = ' ';
            buffer[1 + width + 2] = (char) ('0' + (value / 10));
            buffer[1 + width + 3] = (char) ('0' + (value % 10));
        } else {
            buffer[1 + width + 1] = ' ';
            buffer[1 + width + 2] = ' ';
            buffer[1 + width + 3] = (char) ('0' + value);
        }
    }
}
