package com.sdftdusername.pathfinding.commands;

import org.jetbrains.annotations.Nullable;

public class Argument {
    public String fullName;
    public String shortName;
    public boolean optional;
    public ArgumentDataType dataType;
    @Nullable String defaultValue;

    public Argument(String fullName, String shortName, ArgumentDataType dataType) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.dataType = dataType;
        optional = false;
        defaultValue = null;
    }

    public Argument(String fullName, String shortName, ArgumentDataType dataType, @Nullable String defaultValue) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.dataType = dataType;
        optional = true;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        String open;
        String close = switch (dataType) {
            case STRING -> {
                open = "[";
                yield "]";
            }
            case NUMBER -> {
                open = "(";
                yield ")";
            }
            case BOOLEAN -> {
                open = "<";
                yield ">";
            }
            default -> {
                open = "";
                yield "";
            }
        };

        return open + fullName + (optional ? "?" : "") + close;
    }
}
