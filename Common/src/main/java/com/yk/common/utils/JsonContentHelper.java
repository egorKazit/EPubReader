package com.yk.common.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/**
 * Json converter
 */
public class JsonContentHelper {
    public static JSONObject getContentInJson(InputStream inputStream) {
        String contentInXml =
                new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining());
        return new XmlToJson.Builder(contentInXml).build().toJson();
    }

}