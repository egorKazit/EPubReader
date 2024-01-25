package com.yk.common.model.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Root(name = "ncx", strict = false)
public class NavigationControl {

    private Head head = new Head();
    @Element(name = "navMap", required = false)
    private NavigationMap navigationMap = new NavigationMap();

    @NoArgsConstructor
    @Getter
    @Setter
    @Root(strict = false)
    public static class Head {
        @ElementList(entry = "meta", inline = true)
        private List<Meta> meta = new ArrayList<>();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @Root(strict = false)
    public static class Meta {
        @Attribute
        private String name = "";
        @Attribute
        private String content = "";
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @Root(strict = false)
    public static class NavigationMap {
        @ElementList(entry = "navPoint", inline = true, required = false, data = true)
        private List<NavigationPoint> navigationPoints = new ArrayList<>();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @Root(strict = false)
    public static class NavigationPoint {
        @Attribute(name = "class", required = false)
        private String mClass;
        @ElementList(entry = "text", inline = true, required = false)
        @Path("navLabel")
        private List<String> text = new ArrayList<>();
        @Attribute(required = false)
        private String id = "";
        @Attribute(required = false)
        private String playOrder = "";
        @Attribute(name = "src", required = false)
        @Path("content")
        private String content = "";
        @ElementList(entry = "navPoint", inline = true, required = false)
        private List<NavigationPoint> navigationPoints = new ArrayList<>();
    }

}
