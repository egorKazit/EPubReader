package com.yk.common.constants;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("SpellCheckingInspection")
public enum ContentFont {
    DEFAULT("0", "Default", null),
    SABON("1", "Sabon", "sabon.ttf"),
    BASKERVILLE("2", "Baskerville", "BaskervilleBT.ttf"),
    SALSON("3", "Caslon", "LibreCaslonText-Regular.ttf"),
    GARAMOND("4", "Garamond", "garamond.ttf"),
    UTOPIA("5", "Utopia", "utopia.ttf");
    private final String id;
    private final String fontName;
    private final String fontTechnicalName;

    public static ContentFont valueOfContentFontId(String contentFontId) {
        return Arrays.stream(values()).filter(contentFont -> contentFont.getId().equals(contentFontId)).findAny().orElse(DEFAULT);
    }
}