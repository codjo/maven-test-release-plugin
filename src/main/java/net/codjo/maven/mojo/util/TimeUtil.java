package net.codjo.maven.mojo.util;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
/**
 * TODO : move this to codjo-util's TimeUtil class.
 */
public class TimeUtil {
    /**
     * Parse a duration represented by the string parameter. The value might be : <ol> <li>a number representing a
     * number of milliseconds.</li> <li>a string that looks like "2 hours 3 minutes".</li> <li>a string that looks like
     * "2h3m" or "2h 3m".</li> </ol>
     */
    public static Duration parseDuration(String duration) {
        Duration result;
        try {
            result = new Duration(Long.parseLong(duration));
        }
        catch (NumberFormatException nfe) {
            try {
                result = LONG_PERIOD_FORMATTER.parsePeriod(duration).toStandardDuration();
            }
            catch (IllegalArgumentException iae) {
                result = SHORT_PERIOD_FORMATTER.parsePeriod(duration).toStandardDuration();
            }
        }
        return result;
    }


    private static final PeriodFormatter SHORT_PERIOD_FORMATTER = createPeriodFormatter("h", "m", "s", false);
    private static final PeriodFormatter LONG_PERIOD_FORMATTER = createPeriodFormatter("hour",
                                                                                       "minute",
                                                                                       "second",
                                                                                       true);


    private static PeriodFormatter createPeriodFormatter(String hourSuffix,
                                                         String minuteSuffix,
                                                         String secondSuffix,
                                                         boolean fullNameSuffix) {
        String[] variants = {" ", ",", ",and ", ", and "};

        PeriodFormatterBuilder formatterBuilder = new PeriodFormatterBuilder();
        formatterBuilder.appendYears();
        formatterBuilder.appendSuffix(" year", " years");
        formatterBuilder.appendSeparator(", ", " and ", variants);
        formatterBuilder.appendMonths();
        formatterBuilder.appendSuffix(" month", " months");
        formatterBuilder.appendSeparator(", ", " and ", variants);
        formatterBuilder.appendWeeks();
        formatterBuilder.appendSuffix(" week", " weeks");
        formatterBuilder.appendSeparator(", ", " and ", variants);
        formatterBuilder.appendDays();
        formatterBuilder.appendSuffix(" day", " days");
        formatterBuilder.appendSeparator(", ", " and ", variants);

        formatterBuilder.appendHours();
        appendSuffix(formatterBuilder, hourSuffix, fullNameSuffix);
        formatterBuilder.appendSeparator(", ", " and ", variants);

        formatterBuilder.appendMinutes();
        appendSuffix(formatterBuilder, minuteSuffix, fullNameSuffix);
        formatterBuilder.appendSeparator(", ", " and ", variants);

        formatterBuilder.appendSeconds();
        appendSuffix(formatterBuilder, secondSuffix, fullNameSuffix);
        formatterBuilder.appendSeparator(", ", " and ", variants);

        formatterBuilder.appendMillis();
        formatterBuilder.appendSuffix(" millisecond", " milliseconds");
        return formatterBuilder.toFormatter();
    }


    private static PeriodFormatterBuilder appendSuffix(PeriodFormatterBuilder formatterBuilder,
                                                       String fieldSuffix,
                                                       boolean fullNameSuffix) {
        if (fullNameSuffix && !fieldSuffix.startsWith(" ")) {
            fieldSuffix = " " + fieldSuffix;
        }
        if (fullNameSuffix) {
            formatterBuilder.appendSuffix(fieldSuffix, fieldSuffix + 's');
        }
        else {
            formatterBuilder.appendSuffix(fieldSuffix);
        }
        return formatterBuilder;
    }


    public static final StringBuffer printTo(StringBuffer buffer, Duration duration) {
        LONG_PERIOD_FORMATTER.printTo(buffer, duration.toPeriod());
        return buffer;
    }
}
