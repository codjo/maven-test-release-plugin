package net.codjo.maven.mojo.util;
import junit.framework.TestCase;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
/**
 * Tests for class {@link TimeUtil}.
 */
public class TimeUtilTest extends TestCase {
    private static final Duration EXPECTED_DURATION = Hours.FIVE
          .toStandardDuration()
          .plus(Minutes.THREE.toStandardDuration());


    public void testParseDuration_number() throws Exception {
        testParseDuration(Long.toString(EXPECTED_DURATION.getMillis()));
    }


    public void testParseDuration_nullString() throws Exception {
        testParseDuration(null);
    }


    public void testParseDuration_longString_withAnd() throws Exception {
        testParseDuration("5 hours and 3 minutes");
    }


    public void testParseDuration_longString_withoutAnd() throws Exception {
        testParseDuration("5 hours 3 minutes");
    }


    public void testParseDuration_shortString() throws Exception {
        testParseDuration("5h 3m");
    }


    public void testParseDuration_compactString() throws Exception {
        testParseDuration("5h3m");
    }


    private void testParseDuration(String duration) throws Exception {
        Duration actualDuration = TimeUtil.parseDuration(duration);
        Duration expectedDuration = (duration == null) ? null : EXPECTED_DURATION;
        assertEquals(expectedDuration, actualDuration);
    }


    public void testPrintTo_nonNullDuration() throws Exception {
        testPrintTo(EXPECTED_DURATION, "5 hours and 3 minutes");
    }


    public void testPrintTo_nullDuration() throws Exception {
        testPrintTo(null, "null");
    }


    private void testPrintTo(Duration duration, String expectedString) throws Exception {
        StringBuffer buffer = new StringBuffer();
        String actualResult = TimeUtil.printTo(buffer, duration).toString();
        assertEquals(expectedString, actualResult);
    }
}
