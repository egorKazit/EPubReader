package com.yk.common.utils;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;

/**
 * Java script interactor.
 * It allows to set js scripts in web view and retrieves result of script
 */
public class JavaScriptInteractor {

    private final WebView webView;

    /**
     * Main constructor
     *
     * @param webView web view to which js functions should be added
     */
    public JavaScriptInteractor(WebView webView) {
        this.webView = webView;
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(webView.getClass().getName(), consoleMessage.message());
                return true;
            }
        });
    }

    /**
     * add new interaction
     *
     * @param javascriptInterfaceTag js tag
     * @param consumer               function on js result
     */
    public void addInteraction(JavascriptInterfaceTag javascriptInterfaceTag, Consumer<String> consumer) {
        webView.addJavascriptInterface(new JavascriptInteractionInterface(consumer), javascriptInterfaceTag.name);
    }

    /**
     * Method to set js function
     *
     * @param script js function body
     */
    public void setupScript(String script) {
        webView.evaluateJavascript("(function() { " + script + "; })();", null);
    }

    /**
     * JS interface.
     * Consumer on js result should be provided
     */
    @AllArgsConstructor
    public static class JavascriptInteractionInterface {
        private final Consumer<String> consumer;

        /**
         * JS interface
         *
         * @param result result of performing
         */
        @JavascriptInterface
        public void interact(String result) {
            consumer.accept(result);
        }
    }

    /**
     * Available interfaces
     */
    @AllArgsConstructor
    public enum JavascriptInterfaceTag {
        JAVASCRIPT_CLICK_WORD_INTERFACE("JavascriptClickInteractionInterface"),
        JAVASCRIPT_SELECT_PHRASE_INTERFACE("JavascriptSelectInteractionInterface"),
        JAVASCRIPT_CLICK_PHRASE_INTERFACE("JavascriptPhraseInteractionInterface"),
        JAVASCRIPT_CLICK_IMAGE_INTERFACE("JavascriptImageInteractionInterface");
        private final String name;
    }

}
