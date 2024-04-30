package io.blustream.sulley.mappers.exceptions;

public class MapperException extends Exception {
    public static class InvalidLength extends MapperException {}

    public static class InvalidData extends MapperException {}

    public static class InvalidDate extends MapperException {}

    public static class NotSensorValid extends MapperException {}
}
