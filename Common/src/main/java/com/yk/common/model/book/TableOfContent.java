package com.yk.common.model.book;


import static com.yk.common.constants.Tags.CONTENT;
import static com.yk.common.constants.Tags.ID;
import static com.yk.common.constants.Tags.ITEM;
import static com.yk.common.constants.Tags.ITEM_REF;
import static com.yk.common.constants.Tags.MANIFEST;
import static com.yk.common.constants.Tags.NAVIGATION_LABEL;
import static com.yk.common.constants.Tags.NAVIGATION_MAP;
import static com.yk.common.constants.Tags.NAVIGATION_POINT;
import static com.yk.common.constants.Tags.NCX;
import static com.yk.common.constants.Tags.PACKAGE;
import static com.yk.common.constants.Tags.SOURCE;
import static com.yk.common.constants.Tags.SPINE;
import static com.yk.common.constants.Tags.TEXT;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class TableOfContent {

    private final LinkedList<Chapter> chapterTree = new LinkedList<>();
    private final LinkedList<Spine> spines = new LinkedList<>();
    @Getter(AccessLevel.PUBLIC)
    private String cover;


    private TableOfContent() {
    }

    static TableOfContent fromJson(@NonNull JSONObject tableOfContentInJson,
                                   @NonNull JSONObject contentFileInJson,
                                   @NonNull String coverId) throws JSONException {
        TableOfContent tableOfContent = new TableOfContent();
        setSpineAndCover(contentFileInJson, tableOfContent, coverId);
        setChapterTree(tableOfContentInJson, tableOfContent);
        return tableOfContent;
    }


    private static void setSpineAndCover(JSONObject contentFileInJson,
                                         TableOfContent tableOfContent,
                                         String coverId) throws JSONException {
        JSonSpine[] jSonSpine =
                new Gson().fromJson(contentFileInJson.getJSONObject(PACKAGE.getTag()).getJSONObject(SPINE.getTag())
                        .getJSONArray(ITEM_REF.getTag()).toString(), JSonSpine[].class);
        JSonManifest[] jSonManifest =
                new Gson().fromJson(contentFileInJson.getJSONObject(PACKAGE.getTag()).getJSONObject(MANIFEST.getTag())
                        .getJSONArray(ITEM.getTag()).toString(), JSonManifest[].class);
        Optional<JSonManifest> coverJSonManifest = Stream.of(jSonManifest).filter(jSonManifestForCover -> jSonManifestForCover.id.equals(coverId)).findAny();
        coverJSonManifest.ifPresent(sonManifest -> tableOfContent.cover = sonManifest.href);
        Stream.of(jSonSpine).forEach(jSonSpineEntry -> {
            Optional<JSonManifest> jSonManifestForSpine = Stream
                    .of(jSonManifest)
                    .filter(jSonManifestEntry -> jSonManifestEntry.id.equals(jSonSpineEntry.idRef))
                    .findFirst();
            Spine.SpineBuilder spineBuilder = Spine.builder()
                    .spineId(tableOfContent.spines.size());
            jSonManifestForSpine.ifPresent(sonManifest -> spineBuilder.chapterRef(sonManifest.href)
                    .mediaType(sonManifest.mediaType));
            tableOfContent.spines.add(spineBuilder.build());
        });
    }

    private static void setChapterTree(JSONObject tableOfContentInJson,
                                       TableOfContent tableOfContent) throws JSONException {
        JSONArray navigational = tableOfContentInJson.getJSONObject(NCX.getTag())
                .getJSONObject(NAVIGATION_MAP.getTag())
                .getJSONArray(NAVIGATION_POINT.getTag());
        tableOfContent.chapterTree.addAll(mapJsonToChapters(navigational, tableOfContent));
    }

    private static List<Chapter> mapJsonToChapters(JSONArray chaptersInJson,
                                                   TableOfContent tableOfContent) throws JSONException {
        List<Chapter> localChapters = new ArrayList<>();
        for (int i = 0; i < chaptersInJson.length(); i++) {
            JSONObject chapterInJson = chaptersInJson.getJSONObject(i);
            Chapter.ChapterBuilder chapterBuilder = Chapter.builder()
                    .chapterId(chapterInJson.getString(ID.getTag()))
                    .chapterName(
                            chapterInJson.getJSONObject(NAVIGATION_LABEL.getTag()).getString(TEXT.getTag()))
                    .chapterRef(chapterInJson.getJSONObject(CONTENT.getTag()).getString(SOURCE.getTag()).split("#")[0]);
            if (chapterInJson.has(NAVIGATION_POINT.getTag())) {
                chapterBuilder.subChapters(new LinkedList<>(mapJsonToChapters(chapterInJson.getJSONArray(NAVIGATION_POINT.getTag()), tableOfContent)));
            } else {
                chapterBuilder.subChapters(new LinkedList<>());
            }
            Optional<Spine> spineDef = tableOfContent.spines.stream().filter(spine -> spine.chapterRef.equals(chapterBuilder.chapterRef)).findFirst();
            spineDef.ifPresent(spine -> chapterBuilder.spineRefId(spine.spineId));
            localChapters.add(chapterBuilder.build());
        }
        return localChapters;
    }

    public Spine getSpineById(int id) {
        return spines.get(id);
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Chapter {

        private final String chapterId;
        private final String chapterName;
        private final String chapterRef;
        private final int spineRefId;
        private final LinkedList<Chapter> subChapters;

    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Spine {
        private final int spineId;
        private final String chapterRef;
        private final String mediaType;
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class JSonManifest {
        @SerializedName("id")
        final String id;
        @SerializedName("href")
        final String href;
        @SerializedName("media-type")
        final String mediaType;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class JSonSpine {
        @SerializedName("idref")
        final String idRef;
    }

}