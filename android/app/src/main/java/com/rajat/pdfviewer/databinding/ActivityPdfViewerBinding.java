package com.rajat.pdfviewer.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;

import com.example.sharenote.R;
import com.rajat.pdfviewer.PdfRendererView;

public final class ActivityPdfViewerBinding implements ViewBinding {
    @NonNull
    private final LinearLayout rootView;

    @NonNull
    public final FrameLayout mainLayout;

    @NonNull
    public final Toolbar myToolbar;

    @NonNull
    public final LinearLayout parentLayout;

    @NonNull
    public final PdfRendererView pdfView;

    @NonNull
    public final ProgressBar progressBar;

    @NonNull
    public final TextView tvAppBarTitle;

    private ActivityPdfViewerBinding(@NonNull LinearLayout rootView, @NonNull FrameLayout mainLayout,
                                     @NonNull Toolbar myToolbar, @NonNull LinearLayout parentLayout,
                                     @NonNull PdfRendererView pdfView, @NonNull ProgressBar progressBar,
                                     @NonNull TextView tvAppBarTitle) {
        this.rootView = rootView;
        this.mainLayout = mainLayout;
        this.myToolbar = myToolbar;
        this.parentLayout = parentLayout;
        this.pdfView = pdfView;
        this.progressBar = progressBar;
        this.tvAppBarTitle = tvAppBarTitle;
    }

    @Override
    @NonNull
    public LinearLayout getRoot() {
        return rootView;
    }

    @NonNull
    public static ActivityPdfViewerBinding inflate(@NonNull LayoutInflater inflater) {
        return inflate(inflater, null, false);
    }

    @NonNull
    public static ActivityPdfViewerBinding inflate(@NonNull LayoutInflater inflater,
                                                   @Nullable ViewGroup parent, boolean attachToParent) {
        View root = inflater.inflate(R.layout.activity_pdf_viewer, parent, false);
        if (attachToParent) {
            parent.addView(root);
        }
        return bind(root);
    }

    @NonNull
    public static ActivityPdfViewerBinding bind(@NonNull View rootView) {
        // The body of this method is generated in a way you would not otherwise write.
        // This is done to optimize the compiled bytecode for size and performance.
        int id;
        missingId: {
            id = R.id.mainLayout;
            FrameLayout mainLayout = ViewBindings.findChildViewById(rootView, id);
            if (mainLayout == null) {
                break missingId;
            }

            id = R.id.my_toolbar;
            Toolbar myToolbar = ViewBindings.findChildViewById(rootView, id);
            if (myToolbar == null) {
                break missingId;
            }

            LinearLayout parentLayout = (LinearLayout) rootView;

            id = R.id.pdfView;
            PdfRendererView pdfView = ViewBindings.findChildViewById(rootView, id);
            if (pdfView == null) {
                break missingId;
            }

            id = R.id.progressBar;
            ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
            if (progressBar == null) {
                break missingId;
            }

            id = R.id.tvAppBarTitle;
            TextView tvAppBarTitle = ViewBindings.findChildViewById(rootView, id);
            if (tvAppBarTitle == null) {
                break missingId;
            }

            return new ActivityPdfViewerBinding((LinearLayout) rootView, mainLayout, myToolbar,
                    parentLayout, pdfView, progressBar, tvAppBarTitle);
        }
        String missingId = rootView.getResources().getResourceName(id);
        throw new NullPointerException("Missing required view with ID: ".concat(missingId));
    }
}