package com.yk.bookviewer.ui.dictionary;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.yk.bookviewer.R;
import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.Toaster;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Definition fragment.
 * It's the next step after a dictionary fragment and can be reached only from the dictionary fragment.
 * The fragment contains additional information as list of definitions
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class DictionaryDefinitionFragment extends Fragment {

    private Dictionary dictionary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() == null)
            return super.onCreateView(inflater, container, savedInstanceState);

        // get dictionary from a position. The position is provided from arguments
        var futureDictionary = Executors.newSingleThreadExecutor()
                .submit(() -> DictionaryService.getInstance().getDictionaries().get(getArguments().getInt(GlobalConstants.ORIGIN_WORD_POSITION)));
        try {
            dictionary = futureDictionary.get();
        } catch (ExecutionException | InterruptedException exception) {
            Toaster.make(requireContext(), "Can not load definitions", exception);
            this.requireActivity().onBackPressed();
        }
        // inflate view and put data in
        View rootView = inflater.inflate(R.layout.fragment_definitions, container, false);
        TextView originWord = rootView.findViewById(R.id.dictionaryOriginWord);
        originWord.setText(dictionary.getOriginWord().getOriginWord());
        TextView targetWord = rootView.findViewById(R.id.dictionaryTargetWord);
        targetWord.setText(DictionaryService.getMainTranslation(dictionary));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // set definitions via translate operator
        var dictionaryDefinitionFragmentOperator = new DictionaryDefinitionFragmentOperator(this.getActivity());
        if (dictionary.getDefinitions() != null && !dictionary.getDefinitions().isEmpty()) {
            var valuesWithPartOfSpeech = dictionary.getDefinitions().stream()
                    .map(wordDefinition -> Map.entry(wordDefinition.getDefinition(), wordDefinition.getPartOfSpeech()))
                    .collect(Collectors.toSet());
            dictionaryDefinitionFragmentOperator.inflateValuesWithPartOfSpeech(R.id.dictionaryDetailsDefinitions, valuesWithPartOfSpeech);
        }
        if (dictionary.getTranslations() != null && !dictionary.getTranslations().isEmpty()) {
            var valuesWithPartOfSpeech = dictionary.getTranslations().stream()
                    .map(wordTranslation -> Map.entry(wordTranslation.getTranslation(), wordTranslation.getPartOfSpeech()))
                    .collect(Collectors.toSet());
            dictionaryDefinitionFragmentOperator.inflateValuesWithPartOfSpeech(R.id.dictionaryDetailsTranslations, valuesWithPartOfSpeech);
        }
    }

}
