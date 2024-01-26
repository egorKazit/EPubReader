package com.yk.common.model.xml;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Root(name = "package", strict = false)
public class Package {

    @Element(name = "spine")
    private Spine spine = new Spine();
    @Element(name = "manifest")
    private Manifest manifest = new Manifest();
    @Element(name = "metadata")
    private Metadata metadata = new Metadata();

    @NoArgsConstructor
    @Setter
    @Getter
    @Root(strict = false)
    public static class Spine {
        @SuppressWarnings("SpellCheckingInspection")
        @SerializedName("itemref")
        @ElementList(entry = "itemref", inline = true)
        private List<IdRef> itemRef = new ArrayList<>();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    @Root(strict = false)
    public static class IdRef {
        @SuppressWarnings("SpellCheckingInspection")
        @Attribute(name = "idref")
        private String idRef = "";
    }

    @NoArgsConstructor
    @Setter
    @Getter
    @Root(strict = false)
    public static class Manifest {
        @ElementList(entry = "item", inline = true)
        private List<Item> item = new ArrayList<>();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    @Root(strict = false)
    public static class Item {
        @Attribute
        private String href = "";
        @Attribute
        private String id = "";
        @Attribute(name = "media-type")
        private String mediaType = "";
    }

    @NoArgsConstructor
    @Setter
    @Getter
    @Root(strict = false)
    public static class Metadata {
        @Element(name = "language", required = false)
        private String language = "";
        @Element(name = "title", required = false)
        private String title = "";
        @ElementList(entry = "creator", inline = true, required = false)
        private List<String> creators = new ArrayList<>();
        @ElementList(entry = "meta", inline = true)
        private List<Meta> meta = new ArrayList<>();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    @Root(strict = false)
    public static class Meta {
        @Attribute(name = "name", required = false)
        private String name = "";
        @Attribute(name = "content", required = false)
        private String content = "";
    }

}
