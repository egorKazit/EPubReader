package com.yk.common.model.dictionary;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Parts of speech.
 * Currently implemented only for english
 */
@AllArgsConstructor
@Getter
public enum PartOfSpeech {
    NONE("None", "-none"),
    VERB("Verb", "-v"),
    NOUN("Noun", "-n"),
    ADJECTIVE("Adjective", "-adj"),
    DETERMINER("Determiner", "dtr"),
    ADVERB("Adverb", "adv"),
    PRONOUN("Pronoun", "-pron"),
    PREPOSITION("Preposition", "-prep"),
    CONJUNCTION("Conjunction", "cnj"),
    INTERJECTION("Interjection", "interj");

    private final String name;
    private final String abbreviation;

    /**
     * Method to get abbreviation
     *
     * @param name part of speech
     * @return abbreviation
     */
    public static String getAbbreviationFromName(String name) {
        return Arrays.stream(PartOfSpeech.values())
                .filter(partOfSpeech -> partOfSpeech.name.equalsIgnoreCase(name))
                .findAny().orElse(NONE).abbreviation;
    }

}
