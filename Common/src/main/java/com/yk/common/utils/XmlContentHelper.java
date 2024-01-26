package com.yk.common.utils;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Json converter
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XmlContentHelper {

    private final static Pattern pattern = Pattern.compile("class=('.*?'|\".*?\")");

    public static <T> T getXmlContentFromInputStream(InputStream inputStream, Class<T> targetType) throws Exception {
        String contentInXml = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining());
        contentInXml = pattern.matcher(contentInXml).replaceAll("");
        Serializer serializer = new Persister();
        return serializer.read(targetType, new StringReader(contentInXml), false);
    }

}