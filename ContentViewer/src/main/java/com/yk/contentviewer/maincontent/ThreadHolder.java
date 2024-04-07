package com.yk.contentviewer.maincontent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class ThreadHolder {
    static final ExecutorService wordTranslationThreadOperator = Executors.newSingleThreadExecutor();
    static final ExecutorService phraseTranslationThreadOperator = Executors.newSingleThreadExecutor();
}
