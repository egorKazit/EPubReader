package com.yk.common.model.book;


import com.yk.common.model.xml.NavigationControl;
import com.yk.common.model.xml.Package;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableOfContent {

    private final LinkedList<Chapter> chapterTree = new LinkedList<>();
    private final LinkedList<Spine> spines = new LinkedList<>();

    public static TableOfContent fromNavigationControl(@NonNull NavigationControl navigationControl,
                                                       @NonNull Package xmlPackage) {
        TableOfContent tableOfContent = new TableOfContent();
        setSpineAndCover(xmlPackage, tableOfContent);
        setChapterTree(navigationControl, tableOfContent);
        return tableOfContent;
    }

    private static void setSpineAndCover(@NonNull Package xmlPackage,
                                         @NonNull TableOfContent tableOfContent) {
        xmlPackage.getSpine().getItemRef()
                .forEach(itemRef -> {
                    Optional<Package.Item> itemOptional = xmlPackage.getManifest().getItem().stream()
                            .filter(item -> item.getId().equals(itemRef.getIdRef())).findFirst();
                    Spine.SpineBuilder spineBuilder = Spine.builder()
                            .spineId(tableOfContent.spines.size());
                    itemOptional.ifPresent(item -> spineBuilder.chapterRef(item.getHref())
                            .mediaType(item.getMediaType()));
                    tableOfContent.spines.add(spineBuilder.build());
                });
    }

    private static void setChapterTree(NavigationControl navigationControl,
                                       TableOfContent tableOfContent) {
        if (navigationControl.getNavigationMap() == null) return;
        var navigationPoints = navigationControl.getNavigationMap().getNavigationPoints();
        tableOfContent.chapterTree.addAll(mapNavigationToChapters(navigationPoints, tableOfContent));
    }

    private static List<Chapter> mapNavigationToChapters(List<NavigationControl.NavigationPoint> navigationPoints,
                                                         TableOfContent tableOfContent) {

        AtomicInteger index = new AtomicInteger();
        return navigationPoints.stream().map(navigationPoint -> {
            var chapterBuilder = Chapter.builder()
                    .chapterId(navigationPoint.getId() != null ? navigationPoint.getId() : "local_test_chapter" + index.getAndIncrement())
                    .chapterName(String.join(" ", navigationPoint.getText())).chapterRef(navigationPoint.getContent().split("#")[0]);
            if (navigationPoint.getNavigationPoints() != null && !navigationPoint.getNavigationPoints().isEmpty()) {
                chapterBuilder.subChapters(new LinkedList<>(mapNavigationToChapters(navigationPoint.getNavigationPoints(), tableOfContent)));
            } else {
                chapterBuilder.subChapters(new LinkedList<>());
            }
            Optional<Spine> spineDef = tableOfContent.spines.stream().filter(spine -> spine.chapterRef.equals(chapterBuilder.chapterRef)).findFirst();
            spineDef.ifPresent(spine -> chapterBuilder.spineRefId(spine.spineId));
            return chapterBuilder.build();
        }).collect(Collectors.toList());
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

}