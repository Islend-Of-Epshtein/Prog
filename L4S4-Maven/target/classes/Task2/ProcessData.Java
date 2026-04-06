package Task2;

import java.util.Locale;

public class ProcessData {
    private final double temperature;
    private final double pressure;

    public ProcessData(double temperature, double pressure) {
        this.temperature = temperature;
        this.pressure = pressure;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public String serialize() {
        return String.format(Locale.US, "%.2f;%.2f", temperature, pressure);
    }

    public static ProcessData deserialize(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Пустая строка данных");
        }

        String[] parts = line.split(";");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Неверный формат данных: " + line);
        }

        double temperature = Double.parseDouble(parts[0].trim());
        double pressure = Double.parseDouble(parts[1].trim());

        return new ProcessData(temperature, pressure);
    }

    @Override
    public String toString() {
        return "Температура: " + String.format(Locale.US, "%.2f", temperature)
                + " °C, Давление: " + String.format(Locale.US, "%.2f", pressure) + " атм";
    }
}