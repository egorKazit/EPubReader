package com.yk.common.model.dictionary;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Dictionary definitions
 */
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
public final class Dictionary {

    @Embedded
    private final OriginWord originWord;
    @Relation(parentColumn = "id", entityColumn = "origin_word_id")
    private final List<WordTranslation> translations;
    @Relation(parentColumn = "id", entityColumn = "origin_word_id")
    private final List<WordDefinition> definitions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dictionary that = (Dictionary) o;
        return Objects.equals(originWord.getId(), that.originWord.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(originWord);
    }
}
