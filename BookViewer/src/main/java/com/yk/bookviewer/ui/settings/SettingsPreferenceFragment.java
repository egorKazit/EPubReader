package com.yk.bookviewer.ui.settings;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.yk.bookviewer.R;
import com.yk.common.learning.LearningOperator;
import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.ContentFont;
import com.yk.common.utils.PreferenceHelper;

import java.util.Arrays;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.S)
public class SettingsPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.settoings_preferences, rootKey);
        SwitchPreferenceCompat learningSwitch = findPreference("learning");
        if (learningSwitch != null) {
            learningSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceHelper.Instance.INSTANCE.helper.setLearningEnabled((Boolean) newValue);
                if ((boolean) newValue)
                    new LearningOperator(ApplicationContext.getContext()).startLearning();
                else {
                    new LearningOperator(ApplicationContext.getContext()).stopLearning();
                }
                return true;
            });
            learningSwitch.setChecked(PreferenceHelper.Instance.INSTANCE.helper.isLearningEnabled());
        }
        ListPreference learningIntervalList = findPreference("learning_delay");
        if (learningIntervalList != null) {
            learningIntervalList.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceHelper.Instance.INSTANCE.helper.setLearningInterval(Integer.parseInt((String) newValue));
                return true;
            });
        }

        SwitchPreferenceCompat darkModeSwitch = findPreference("nightMode");
        if (darkModeSwitch != null) {
            darkModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                var newValueInCorrectType = (Boolean) newValue;
                darkModeSwitch.setChecked(newValueInCorrectType);
                PreferenceHelper.Instance.INSTANCE.helper.enableNightMode(!newValueInCorrectType);
                return true;
            });
            darkModeSwitch.setChecked(PreferenceHelper.Instance.INSTANCE.helper.isNightMode());
        }

        ListPreference fontSettings = findPreference("font");
        if (fontSettings != null) {
            CharSequence[] entries = Arrays
                    .stream(ContentFont.values()).map(contentFont -> (CharSequence) contentFont.getFontName())
                    .collect(Collectors.toList()).toArray(new CharSequence[]{});
            CharSequence[] entryValues = Arrays
                    .stream(ContentFont.values()).map(contentFont -> (CharSequence) contentFont.getId())
                    .collect(Collectors.toList()).toArray(new CharSequence[]{});
            fontSettings.setEntries(entries);
            fontSettings.setValue(PreferenceHelper.Instance.INSTANCE.helper.getContentFont().getId());
            fontSettings.setEntryValues(entryValues);
            fontSettings.setOnPreferenceChangeListener((preference, newValue) -> {
                ContentFont contentFont = ContentFont.valueOfContentFontId((String) newValue);
                PreferenceHelper.Instance.INSTANCE.helper.setContentFont(contentFont);
                return true;
            });
        }
    }
}
