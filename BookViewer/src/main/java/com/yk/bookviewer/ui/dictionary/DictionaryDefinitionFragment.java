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
import com.yk.common.model.dictionary.DictionaryPool;

/**
 * Definition fragment.
 * It's the next step after a dictionary fragment and can be reached only from the dictionary fragment.
 * The fragment contains additional information as list of definitions
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class DictionaryDefinitionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() == null)
            return super.onCreateView(inflater, container, savedInstanceState);

        // get dictionary from a position. The position is provided from arguments
        Dictionary dictionary = DictionaryPool.getDictionaries().get(getArguments().getInt(GlobalConstants.ORIGIN_WORD_POSITION));
        // inflate view and put data in
        View rootView = inflater.inflate(R.layout.fragment_definitions, container, false);
        TextView originWord = rootView.findViewById(R.id.dictionaryOriginWord);
        originWord.setText(dictionary.getOriginWord().getOriginWord());
        TextView targetWord = rootView.findViewById(R.id.dictionaryTargetWord);
        targetWord.setText(dictionary.getTranslations().get(0).getTranslation());
        // set definitions via translate operator
        new DictionaryDefinitionOperator(rootView, dictionary.getOriginWord().getOriginWord()).start();
        return rootView;
    }
}
