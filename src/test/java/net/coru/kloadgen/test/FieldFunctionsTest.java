package net.coru.kloadgen.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Created by satish on 5/3/17.
 */
public class FieldFunctionsTest {

    @Test
    public void verifyTimeFunctions() {

        assertThat(FieldDataFunctions.TIMESTAMP() <= System.currentTimeMillis()).as("Invalid timestamp value").isTrue() ;
        assertThat(FieldDataFunctions.TIMESTAMP("01-05-1988 12:12:12", "01-05-2000 12:12:12") > 568363332000L).as("Invalid timestamp between interval value").isTrue();
        assertThat(FieldDataFunctions.DATE("dd-MM-yyyy HH:mm:ss")).as("Invalid date value").isNotNull();

    }

    @Test
    public void verifyRandomFunctions() {

        assertThat(FieldDataFunctions.RANDOM_STRING("1", "2")).as("Invalid random string value").isNotNull();
        assertThat(FieldDataFunctions.RANDOM_INT(1, 2)).as("Invalid int value").isPositive();
        assertThat(FieldDataFunctions.RANDOM_DOUBLE(1.0, 2.0)).as("Invalid double value").isPositive();
        assertThat(FieldDataFunctions.RANDOM_FLOAT(1.0F, 2.0F)).as("Invalid float value").isPositive();
        assertThat(FieldDataFunctions.RANDOM_LONG(1, 2)).as("Invalid long value").isPositive();
        assertThat(FieldDataFunctions.RANDOM_ALPHA_NUMERIC("A", 3)).as("Invalid random string").isEqualTo("AAA");


    }

    @Test
    public void verifyRandomRangeFunctions() {

        assertThat(FieldDataFunctions.RANDOM_INT_RANGE(1, 3)).as("Invalid int range value").isLessThan(3);
        assertThat(FieldDataFunctions.RANDOM_LONG_RANGE(1, 3)).as("Invalid long range value").isLessThan(3);
        assertThat(FieldDataFunctions.RANDOM_FLOAT_RANGE(1, 3)).as("Invalid float range value").isLessThan(3);
        assertThat(FieldDataFunctions.RANDOM_DOUBLE(1, 3)).as("Invalid double range value").isLessThan(3);
    }

    @Test
    public void verifyUserFunctions() {

        assertThat(FieldDataFunctions.FIRST_NAME()).as("Invalid first name value").isNotNull();
        assertThat(FieldDataFunctions.LAST_NAME()).as("Invalid last name value").isNotNull();
        assertThat(FieldDataFunctions.USERNAME()).as("Invalid user name value").isNotNull();
        assertThat(FieldDataFunctions.EMAIL("test.com")).as("Invalid email address value").isNotNull();
        assertThat(FieldDataFunctions.GENDER()).as("Invalid Gender").isNotNull();
        assertThat(FieldDataFunctions.PHONE()).as("Invalid PHONE number").isNotNull();

    }

    @Test
    public void verifyUtilFunctions(){
        assertThat(FieldDataFunctions.UUID()).as("Invalid UUID value").isNotNull();
        assertThat(FieldDataFunctions.IPV4()).as("Invalid IP4 Address").isNotNull();
        assertThat(FieldDataFunctions.IPV6()).as("Invalid IPV6 Address").isNotNull();
        boolean currentBoolean = FieldDataFunctions.BOOLEAN();
        assertThat(currentBoolean == true || currentBoolean == false).as("Invalid boolean value").isTrue();
        assertThat(FieldDataFunctions.SEQUENCE("randomSeq", 1, 1)).as("Invalid sequence").isEqualTo(1);
    }
}
