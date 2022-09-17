package com.yk.common.utils;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ContentFont {
    DEFAULT("0", "Default", null),
    BASKERVILLE("1", "Baskerville", "baskerville.ttf"), SALSON("2", "Caslon", "caslon.otf"),
    GARAMOND("3", "Garamond", "garamond.ttf"), GRYPEN("4", "Grypen", "grypen.ttf"),
    SABON("5", "Sabon", "sabon.ttf"), UTOPIA("6", "Utopia", "utopia.ttf");
    private final String id;
    private final String fontName;
    private final String fontTechnicalName;

    public static ContentFont valueOfContentFontId(String contentFontId) {
        return Arrays.stream(values()).filter(contentFont -> contentFont.getId().equals(contentFontId)).findAny().orElse(DEFAULT);
    }
}