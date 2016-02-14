package com.example.pebble;

import com.mitchellbosecke.pebble.extension.Filter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class SizeFilter implements Filter {
    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return formatFileSize((Long) input);
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    /**
     * Функция перевода количества байтов в форматированную строку
     *
     * @param size количества байтов
     * @return форматированная строка
     */
    private static String formatFileSize(long size) {
        double inBytes = size;
        double inKilobytes = inBytes / 1024.0;
        double inMegabytes = inKilobytes / 1024.0;
        double inGigabytes = inMegabytes / 1024.0;
        double inTerabytes = inGigabytes / 1024.0;

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        String formattedSize;

        if (inTerabytes >= 1) {
            formattedSize = decimalFormat.format(inTerabytes).concat(" TB");
        } else if (inGigabytes >= 1) {
            formattedSize = decimalFormat.format(inGigabytes).concat(" GB");
        } else if (inMegabytes >= 1) {
            formattedSize = decimalFormat.format(inMegabytes).concat(" MB");
        } else if (inKilobytes >= 1) {
            formattedSize = decimalFormat.format(inKilobytes).concat(" KB");
        } else {
            formattedSize = decimalFormat.format(inBytes).concat(" Bytes");
        }

        return formattedSize;
    }
}
