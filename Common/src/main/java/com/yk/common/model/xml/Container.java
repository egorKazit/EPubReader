package com.yk.common.model.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Root(name = "container", strict = false)
public final class Container {

    @Element(name = "rootfiles", required = false)
    private RootFiles rootFiles = new RootFiles();
    @Element(required = false)
    private String version = "0.0";

    @NoArgsConstructor
    @Setter
    @Getter
    public static class RootFiles {
        @SuppressWarnings("SpellCheckingInspection")
        @Element(name = "rootfile", required = false)
        private RootFile rootFile = new RootFile();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class RootFile {
        @Attribute(name = "full-path", required = false)
        private String fullPath = "";
        @Attribute(name = "media-type", required = false)
        private String mediaType = "";
    }

}
