package com.yk.contentviewer.maincontent;

import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.utils.ContentFont;
import com.yk.common.utils.PreferenceHelper;
import com.yk.contentviewer.R;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerWebViewFontRecyclerAdapter extends RecyclerView.Adapter<ContentViewerWebViewFontRecyclerAdapter.ContentViewerWebViewFontHolder> {

    private final List<ContentFont> fontData = List.of(ContentFont.values());
    private int index = 0;
    private final int size = (fontData.size());
    private Runnable runnableBeforeAction;
    private Runnable runnableAfterAction;

    public ContentViewerWebViewFontRecyclerAdapter() {
        super();
        index = Integer.parseInt(PreferenceHelper.Instance.INSTANCE.helper.getContentFont().getId());
    }

    @NonNull
    @Override
    public ContentViewerWebViewFontHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_view_font, parent, false);
        return new ContentViewerWebViewFontHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewerWebViewFontHolder holder, int position) {
        int realIndex = index + position;
        if (realIndex >= size) {
            realIndex -= size;
        }
        ContentFont contentFont = fontData.get(realIndex);
        holder.textView.setText(contentFont.getFontName());
        if (contentFont.getFontTechnicalName() != null) {
            Typeface type = Typeface.createFromAsset(holder.textView.getContext().getAssets(), "fonts/" + contentFont.getFontTechnicalName());
            holder.textView.setTypeface(type);
        } else {
            holder.textView.setTypeface(Typeface.DEFAULT);
        }
        holder.textView.setOnClickListener(v -> {
            runnableBeforeAction.run();
            PreferenceHelper.Instance.INSTANCE.helper.setContentFont(contentFont);
            ViewPager2 viewPager = holder.textView.getRootView().findViewById(R.id.contentViewerChapterPager);
            ContentViewerPagerAdapter contentViewerPagerAdapter = (ContentViewerPagerAdapter) viewPager.getAdapter();
            if (contentViewerPagerAdapter != null)
                contentViewerPagerAdapter.refresh();
            runnableAfterAction.run();
        });
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public ContentViewerWebViewFontRecyclerAdapter setRunnableBeforeAction(Runnable runnableBeforeAction) {
        this.runnableBeforeAction = runnableBeforeAction;
        return this;
    }

    public ContentViewerWebViewFontRecyclerAdapter setRunnableAfterAction(Runnable runnableAfterAction) {
        this.runnableAfterAction = runnableAfterAction;
        return this;
    }

    public void left() {
        if (index > 0)
            index--;
        else if (index == 0)
            index = size - 1;
        notifyItemRangeChanged(0, 3);
    }

    public void right() {
        if (index < size - 1)
            index++;
        else if (index == size - 1)
            index = 0;
        notifyItemRangeChanged(0, 3);
    }

    static class ContentViewerWebViewFontHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ContentViewerWebViewFontHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.recyclerFont);
        }
    }


}
