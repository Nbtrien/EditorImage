package com.example.editor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    MainActivity activity;
    List<Photo> photos;
    ImageView preview;
    BottomSheetBehavior<View> bottomSheetBehavior;
    int selectedPos = 0;

    public PhotoAdapter(MainActivity activity, List<Photo> photos, ImageView preview, BottomSheetBehavior<View> bottomSheetBehavior) {
        this.activity = activity;
        this.photos = photos;
        this.preview = preview;
        this.bottomSheetBehavior = bottomSheetBehavior;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_row_item, parent, false);

        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Photo photo = photos.get(holder.getAdapterPosition());
        PhotoViewHolder viewHolder = (PhotoViewHolder) holder;

        Glide.with(viewHolder.photoView.getContext()).load(Uri.parse(photo.uri))
                .placeholder(R.drawable.placeholder)
                .into(viewHolder.photoView);

        viewHolder.photoView.setOnClickListener(view -> {
            selectedPos = holder.getAdapterPosition();
            notifyDataSetChanged();

            Glide.with(viewHolder.photoView.getContext()).asBitmap()
                    .load(Uri.parse(photo.uri))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            activity.imageView.setImageBitmap(resource);

                            preview.setImageBitmap(resource);
                            activity.ogBmp = new BitmapDrawable(activity.getResources(), resource);
                            activity.filterBmp = null;
                            activity.brightBmp = null;
                            activity.frameType = null;
                            activity.editedBmp = null;
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
            preview.setVisibility(View.VISIBLE);
        });

        if (selectedPos == holder.getAdapterPosition()) {
            viewHolder.photoView.setAlpha(0.25f);
        } else {
            viewHolder.photoView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder{
        ImageView photoView;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }
}
