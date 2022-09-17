package com.yk.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Input stream wrapper with ability to replace occurrences of strings.
 * Initial stream is converted to buffered reader and data is read line by line.
 * Line is assigned to internal buffer, provided patterns are checked and some part is replaced if patterns are matched
 */
public final class InputStreamWrapper extends InputStream {

    private final BufferedReader bufferedReader;
    private final Map<Pattern, String> replacer = new HashMap<>();
    private final Map<String, String> injector = new HashMap<>();

    int pos = 0;
    int count = 0;
    private byte[] buf;
    private final Queue<String> injectedStringQueue = new LinkedBlockingQueue<>();

    /**
     * Main constructor
     *
     * @param inputStream initial stream
     * @throws IOException exception on buffered reader creation
     */
    public InputStreamWrapper(InputStream inputStream) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * Method to initiate replacer.
     * Provided map is converted to internal map with pattern and target string.
     * If value from pattern match, all occurrences will be replaced
     *
     * @param replacerMap replacement map
     * @return itself
     */
    public InputStreamWrapper setReplacer(Map<String, String> replacerMap) {
        replacerMap.forEach((pattern, valueToReplaceWith) -> replacer.put(Pattern.compile(pattern), valueToReplaceWith));
        return this;
    }

    /**
     * Method to initiate injector.
     * Provided map is converted to internal map with expected and target strings.
     * IF expected string is faced, the target line will be added as the next value to read
     *
     * @param injectorMap replacement map
     * @return itself
     */
    public InputStreamWrapper setInjector(Map<String, String> injectorMap) {
        injectorMap.forEach(injector::put);
        return this;
    }

    @Override
    public int read() throws IOException {
        // if out of buffer -> read buffer or the next line
        if (pos >= count) {
            // check if some data is in buffer.
            // if so, then process at the first place
            String currentLine = injectedStringQueue.poll();
            // if no buffer data -> read the next line
            if (currentLine == null)
                currentLine = bufferedReader.readLine();
            // if not line, then finish a reading
            if (currentLine == null)
                return -1;
            // inject/replace if needed
            injectIfNeeded(currentLine);
            currentLine = replaceIfNeeded(currentLine);
            // fill buffer with new line
            buf = currentLine.getBytes(StandardCharsets.UTF_8);
            // update start position and length
            pos = 0;
            count = buf.length;
            if (count == 0) {
                return -1;
            }
        }
        // process regular read
        var bufferValue = buf[pos];
        pos++;
        return bufferValue;
    }

    /**
     * Method to inject additional data if needed
     *
     * @param currentLine current line
     */
    private void injectIfNeeded(String currentLine) {
        injector.forEach((block, stringToInject) -> {
            if (currentLine.contains(block)) {
                injectedStringQueue.add(stringToInject);
            }
        });
    }

    /**
     * Method to replace some part if needed
     *
     * @param currentLine current line
     * @return updated line
     */
    private String replaceIfNeeded(String currentLine) {
        AtomicReference<String> currentLineOut = new AtomicReference<>(currentLine);
        replacer.forEach((pattern, s) -> {
            Matcher matcher = pattern.matcher(currentLine);
            if (matcher.find()) {
                currentLineOut.set(matcher.replaceAll(s));
            }
        });
        return currentLineOut.get();
    }

}
