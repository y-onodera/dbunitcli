package yo.dbunitcli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringsTest {

    @Test
    void isEmptyReturnTrueWhenArgsNull() {
        Assertions.assertTrue(Strings.isEmpty(null));
    }

    @Test
    void isEmptyReturnTrueWhenArgsBlank() {
        Assertions.assertTrue(Strings.isEmpty(""));
    }

    @Test
    void isEmptyReturnFalseWhenArgsBlankAndSpace() {
        Assertions.assertFalse(Strings.isEmpty(" "));
    }

    @Test
    void isEmptyReturnFalseWhenArgsNotBlank() {
        Assertions.assertFalse(Strings.isEmpty("a"));
    }

    @Test
    void isNotEmptyReturnFalseWhenArgsNull() {
        Assertions.assertFalse(Strings.isNotEmpty(null));
    }

    @Test
    void isNotEmptyReturnFalseWhenArgsBlank() {
        Assertions.assertFalse(Strings.isNotEmpty(""));
    }

    @Test
    void isNotEmptyReturnFalseWhenArgsBlankAndSpace() {
        Assertions.assertTrue(Strings.isNotEmpty(" "));
    }

    @Test
    void isNotEmptyReturnTrueWhenArgsNotBlank() {
        Assertions.assertTrue(Strings.isNotEmpty("a"));
    }

    @Test
    void camelToSnakeEnableConvertArgsCamelAndLowerCase() {
        Assertions.assertEquals("camel_to_snake", Strings.camelToSnake("camelToSnake", Character::toLowerCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsCamelAndUpperCase() {
        Assertions.assertEquals("CAMEL_TO_SNAKE", Strings.camelToSnake("camelToSnake", Character::toUpperCase));
    }

    @Test
    void camelToSnakeNotConvertArgsLowerSnakeAndLowerCase() {
        Assertions.assertEquals("camel_to_snake", Strings.camelToSnake("camel_to_snake", Character::toLowerCase));
    }

    @Test
    void camelToSnakeNotConvertArgsUpperSnakeAndUpperCase() {
        Assertions.assertEquals("CAMEL_TO_SNAKE", Strings.camelToSnake("CAMEL_TO_SNAKE", Character::toUpperCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsUpperSnakeAndLowerCase() {
        Assertions.assertEquals("camel_to_snake", Strings.camelToSnake("CAMEL_TO_SNAKE", Character::toLowerCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsLowerSnakeAndUpperCase() {
        Assertions.assertEquals("CAMEL_TO_SNAKE", Strings.camelToSnake("camel_to_snake", Character::toUpperCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsIsWordContainsUpperCaseAndLowerCase() {
        Assertions.assertEquals("camel", Strings.camelToSnake("Camel", Character::toLowerCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsIsWordContainsUpperCaseAndUpperCase() {
        Assertions.assertEquals("CAMEL", Strings.camelToSnake("Camel", Character::toUpperCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsLowerCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.camelToSnake("c", Character::toUpperCase));
    }

    @Test
    void camelToSnakeNotConvertArgsUpperCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.camelToSnake("C", Character::toUpperCase));
    }

    @Test
    void camelToSnakeEnableConvertArgsUpperCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.camelToSnake("C", Character::toLowerCase));
    }

    @Test
    void camelToSnakeNotConvertArgsLowerCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.camelToSnake("c", Character::toLowerCase));
    }

    @Test
    void camelToKebabEnableConvertArgsCamelAndLowerCase() {
        Assertions.assertEquals("camel-to-kebab", Strings.camelToKebab("camelToKebab", Character::toLowerCase));
    }

    @Test
    void camelToKebabEnableConvertArgsCamelAndUpperCase() {
        Assertions.assertEquals("CAMEL-TO-KEBAB", Strings.camelToKebab("camelToKebab", Character::toUpperCase));
    }

    @Test
    void camelToKebabNotConvertArgsLowerKebabAndLowerCase() {
        Assertions.assertEquals("camel-to-kebab", Strings.camelToKebab("camel-to-kebab", Character::toLowerCase));
    }

    @Test
    void camelToKebabNotConvertArgsUpperKebabAndUpperCase() {
        Assertions.assertEquals("CAMEL-TO-KEBAB", Strings.camelToKebab("CAMEL-TO-KEBAB", Character::toUpperCase));
    }

    @Test
    void camelToKebabEnableConvertArgsUpperKebabAndLowerCase() {
        Assertions.assertEquals("camel-to-kebab", Strings.camelToKebab("CAMEL-TO-KEBAB", Character::toLowerCase));
    }

    @Test
    void camelToKebabEnableConvertArgsLowerKebabAndUpperCase() {
        Assertions.assertEquals("CAMEL-TO-KEBAB", Strings.camelToKebab("camel-to-kebab", Character::toUpperCase));
    }

    @Test
    void camelToKebabEnableConvertArgsIsWordContainsUpperCaseAndLowerCase() {
        Assertions.assertEquals("camel", Strings.camelToKebab("Camel", Character::toLowerCase));
    }

    @Test
    void camelToKebabEnableConvertArgsIsWordContainsUpperCaseAndUpperCase() {
        Assertions.assertEquals("CAMEL", Strings.camelToKebab("Camel", Character::toUpperCase));
    }

    @Test
    void camelToKebabEnableConvertArgsLowerCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.camelToKebab("c", Character::toUpperCase));
    }

    @Test
    void camelToKebabNotConvertArgsUpperCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.camelToKebab("C", Character::toUpperCase));
    }

    @Test
    void camelToKebabEnableConvertArgsUpperCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.camelToKebab("C", Character::toLowerCase));
    }

    @Test
    void camelToKebabNotConvertArgsLowerCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.camelToKebab("c", Character::toLowerCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsLowerSnakeAndLowerCase() {
        Assertions.assertEquals("snakeToCamel", Strings.snakeToCamel("snake_to_camel", Character::toLowerCase));
    }

    @Test
    void snakeToCamelNotConvertArgsLowerSnakeAndUpperCase() {
        Assertions.assertEquals("SnakeToCamel", Strings.snakeToCamel("snake_to_camel", Character::toUpperCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsUpperSnakeAndLowerCase() {
        Assertions.assertEquals("snakeToCamel", Strings.snakeToCamel("SNAKE_TO_CAMEL", Character::toLowerCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsUpperSnakeAndUpperCase() {
        Assertions.assertEquals("SnakeToCamel", Strings.snakeToCamel("SNAKE_TO_CAMEL", Character::toUpperCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsIsWordContainsUpperCaseAndLowerCase() {
        Assertions.assertEquals("camel", Strings.snakeToCamel("Camel", Character::toLowerCase));
    }

    @Test
    void snakeToCamelNotConvertArgsIsWordNotContainsUpperCaseAndLowerCase() {
        Assertions.assertEquals("camel", Strings.snakeToCamel("camel", Character::toLowerCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsIsWordContainsUpperCaseAndUpperCase() {
        Assertions.assertEquals("Camel", Strings.snakeToCamel("camel", Character::toUpperCase));
    }

    @Test
    void snakeToCamelNotConvertArgsIsWordStartUpperCaseAndLowerCase() {
        Assertions.assertEquals("Camel", Strings.snakeToCamel("Camel", Character::toUpperCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsLowerCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.snakeToCamel("c", Character::toUpperCase));
    }

    @Test
    void snakeToCamelNotConvertArgsUpperCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.snakeToCamel("C", Character::toUpperCase));
    }

    @Test
    void snakeToCamelEnableConvertArgsUpperCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.snakeToCamel("C", Character::toLowerCase));
    }

    @Test
    void snakeToCamelNotConvertArgsLowerCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.snakeToCamel("c", Character::toLowerCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsLowerKebabAndLowerCase() {
        Assertions.assertEquals("kebabToCamel", Strings.kebabToCamel("kebab-to-camel", Character::toLowerCase));
    }

    @Test
    void kebabToCamelNotConvertArgsLowerKebabAndUpperCase() {
        Assertions.assertEquals("KebabToCamel", Strings.kebabToCamel("kebab-to-camel", Character::toUpperCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsUpperKebabAndLowerCase() {
        Assertions.assertEquals("kebabToCamel", Strings.kebabToCamel("KEBAB-TO-CAMEL", Character::toLowerCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsUpperKebabAndUpperCase() {
        Assertions.assertEquals("KebabToCamel", Strings.kebabToCamel("KEBAB-TO-CAMEL", Character::toUpperCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsIsWordContainsUpperCaseAndLowerCase() {
        Assertions.assertEquals("camel", Strings.kebabToCamel("Camel", Character::toLowerCase));
    }

    @Test
    void kebabToCamelNotConvertArgsIsWordNotContainsUpperCaseAndLowerCase() {
        Assertions.assertEquals("camel", Strings.kebabToCamel("camel", Character::toLowerCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsIsWordContainsUpperCaseAndUpperCase() {
        Assertions.assertEquals("Camel", Strings.kebabToCamel("camel", Character::toUpperCase));
    }

    @Test
    void kebabToCamelNotConvertArgsIsWordStartUpperCaseAndLowerCase() {
        Assertions.assertEquals("Camel", Strings.kebabToCamel("Camel", Character::toUpperCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsLowerCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.kebabToCamel("c", Character::toUpperCase));
    }

    @Test
    void kebabToCamelNotConvertArgsUpperCharAndUpperCase() {
        Assertions.assertEquals("C", Strings.kebabToCamel("C", Character::toUpperCase));
    }

    @Test
    void kebabToCamelEnableConvertArgsUpperCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.kebabToCamel("C", Character::toLowerCase));
    }

    @Test
    void kebabToCamelNotConvertArgsLowerCharAndLowerCase() {
        Assertions.assertEquals("c", Strings.kebabToCamel("c", Character::toLowerCase));
    }

}