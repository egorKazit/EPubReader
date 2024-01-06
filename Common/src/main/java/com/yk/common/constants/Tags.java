package com.yk.common.constants;

import lombok.Getter;

@Getter
public enum Tags {

    CONTAINER("container"), ROOT_FILES("rootfiles"), ROOT_FILE("rootfile"), FULL_PATH("full-path"),
    NCX("ncx"), NAVIGATION_MAP("navMap"), NAVIGATION_POINT("navPoint"), NAVIGATION_LABEL("navLabel"),
    TEXT("text"), ID("id"), CONTENT("content"), SOURCE("src"), PACKAGE("package"), METADATA("metadata"),
    META("meta"), NAME("name"), DESCRIPTION("dc:description"), TITLE("dc:title"), LANGUAGE("dc:language"), CREATOR("dc:creator"),
    MANIFEST("manifest"), SPINE("spine"), ITEM("item"), ITEM_REF("itemref");

    private final String tag;

    Tags(String tag) {
        this.tag = tag;
    }
}
