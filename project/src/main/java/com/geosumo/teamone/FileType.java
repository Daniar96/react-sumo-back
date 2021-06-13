package com.geosumo.teamone;

public enum FileType {
    NODES("nodes", "nodes"),
    EDGES("edges","edges"),
    OUTPUT("fcd-export","output"),
    ROUTES("routes","routes");

    private final String element;
    private final String column;

    FileType(String element, String column) {
        this.element = element;
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public static FileType getType(String element) {
        for (var type : values()) {
            if (type.element.equals(element)) {
                return type;
            }
        }

        return null;
    }
}
