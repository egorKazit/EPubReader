package com.yk.bookviewer.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.yk.bookviewer.R;
import com.yk.common.constants.ContentFont;
import com.yk.common.context.ApplicationContext;
import com.yk.common.service.learning.LearningOperator;
import com.yk.common.utils.PreferenceHelper;

import java.util.Arrays;
import java.util.stream.Collectors;


public class SettingsPreferenceFragment extends PreferenceFragmentCompat {

    public static final String LEARNING = "learning";
    public static final String LEARNING_DELAY = "learning_delay";
    public static final String NIGHT_MODE = "nightMode";
    public static final String FONT = "font";

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
        SwitchPreferenceCompat learningSwitch = findPreference(LEARNING);
        if (learningSwitch != null) {
            learningSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.setLearningEnabled((Boolean) newValue);
                if ((boolean) newValue)
                    new LearningOperator(ApplicationContext.getContext()).startLearning();
                else {
                    new LearningOperator(ApplicationContext.getContext()).stopLearning();
                }
                return true;
            });
            learningSwitch.setChecked(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isLearningEnabled());
        }
        ListPreference learningIntervalList = findPreference(LEARNING_DELAY);
        if (learningIntervalList != null) {
            learningIntervalList.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.setLearningInterval(Integer.parseInt((String) newValue));
                return true;
            });
        }

        SwitchPreferenceCompat darkModeSwitch = findPreference(NIGHT_MODE);
        if (darkModeSwitch != null) {
            darkModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                var newValueInCorrectType = (Boolean) newValue;
                darkModeSwitch.setChecked(newValueInCorrectType);
                PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.enableNightMode(!newValueInCorrectType);
                return true;
            });
            darkModeSwitch.setChecked(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isNightMode());
        }

        ListPreference fontSettings = findPreference(FONT);
        if (fontSettings != null) {
            CharSequence[] entries = Arrays
                    .stream(ContentFont.values()).map(contentFont -> (CharSequence) contentFont.getFontName())
                    .collect(Collectors.toList()).toArray(new CharSequence[]{});
            CharSequence[] entryValues = Arrays
                    .stream(ContentFont.values()).map(contentFont -> (CharSequence) contentFont.getId())
                    .collect(Collectors.toList()).toArray(new CharSequence[]{});
            fontSettings.setEntries(entries);
            fontSettings.setValue(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.getContentFont().getId());
            fontSettings.setEntryValues(entryValues);
            fontSettings.setOnPreferenceChangeListener((preference, newValue) -> {
                ContentFont contentFont = ContentFont.valueOfContentFontId((String) newValue);
                PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.setContentFont(contentFont);
                return true;
            });
        }
    }
}
