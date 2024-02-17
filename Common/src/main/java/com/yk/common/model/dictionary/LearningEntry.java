package com.yk.common.model.dictionary;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LearningEntry {
    private final String originWord;
    private final String correctTranslation;
    private final List<String> possibleTranslations;
}
