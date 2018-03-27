package org.openchs.util;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openchs.application.Form;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class O {
    private static DateTimeFormatter userFormat = DateTimeFormat.forPattern("dd-MMM-yyyy");
    private static String dbFormat = "yyyy-MM-dd";
    private static DateTimeFormatter dbFormatter = DateTimeFormat.forPattern(dbFormat);

    public static String getFullPath(String relativePath) {
        return String.format("file:///%s/", new File(relativePath).getAbsolutePath());
    }

    public static String getDateInDbFormat(Date date) {
        return new SimpleDateFormat(dbFormat).format(date);
    }

    public static Date getDateFromDbFormat(String dbFormat) {
        return LocalDate.parse(dbFormat, dbFormatter).toDate();
    }

    public static Object coalesce(Object... elements) {
        return Stream.of(elements).filter(Objects::nonNull).findFirst().orElse(null);
    }
}