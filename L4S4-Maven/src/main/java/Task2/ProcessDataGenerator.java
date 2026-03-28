package Task2;

import java.util.Random;

public class ProcessDataGenerator {
    private static final double MIN_TEMPERATURE = 0.0;
    private static final double MAX_TEMPERATURE = 100.0;

    private static final double MIN_PRESSURE = 0.0;
    private static final double MAX_PRESSURE = 6.0;

    private final Random random;

    public ProcessDataGenerator() {
        this.random = new Random();
    }

    public ProcessData next() {
        double temperature = MIN_TEMPERATURE
                + (MAX_TEMPERATURE - MIN_TEMPERATURE) * random.nextDouble();

        double pressure = MIN_PRESSURE
                + (MAX_PRESSURE - MIN_PRESSURE) * random.nextDouble();

        return new ProcessData(temperature, pressure);
    }
}