package com.example.editor;

import android.app.Dialog;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class PhotoBottomSheetFragment extends BottomSheetDialogFragment {
    List<Photo> photos;
    BottomSheetDialog dialog;
    BottomSheetBehavior<View> bottomSheetBehavior;
    View rootView;

    public PhotoBottomSheetFragment(List<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.photos_bottom_sheet, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        CoordinatorLayout layout = dialog.findViewById(R.id.bottomSheetLayout);
        assert layout != null;
        layout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);

        TextView closeBtn = rootView.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(view1 -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));

        TextView positiveSelectBtn = rootView.findViewById(R.id.positiveSelectBtn);
        positiveSelectBtn.setOnClickListener(view1 -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));

        ImageView preview = rootView.findViewById(R.id.preview);
        Glide.with(requireActivity()).load(Uri.parse(photos.get(0).uri))
                .placeholder(R.drawable.placeholder)
                .into(preview);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycleView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);
        PhotoAdapter photoAdapter = new PhotoAdapter((MainActivity) getActivity(), photos, preview, bottomSheetBehavior);
        recyclerView.setAdapter(photoAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1)){
                    preview.setVisibility(View.VISIBLE);
                } else {
                    preview.setVisibility(View.GONE);
                }
            }
        });
    }
}
