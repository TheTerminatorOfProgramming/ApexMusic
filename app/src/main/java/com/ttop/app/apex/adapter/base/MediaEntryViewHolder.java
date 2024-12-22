/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ttop.app.apex.adapter.base;

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import com.google.android.material.card.MaterialCardView;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.ttop.app.apex.R;

public class MediaEntryViewHolder extends AbstractDraggableSwipeableItemViewHolder
        implements View.OnLongClickListener, View.OnClickListener {

    @Nullable
    public View dragView;

    @Nullable
    public View dummyContainer;

    @Nullable
    public ImageView image;

    @Nullable
    public AppCompatImageView imagePlaying;

    @Nullable
    public MaterialCardView imageContainerCard;

    @Nullable
    public FrameLayout imageContainer;

    @Nullable
    public TextView imageText;

    @Nullable
    public MaterialCardView imageTextContainer;

    @Nullable
    public View mask;

    @Nullable
    public AppCompatImageView menu;

    @Nullable
    public View paletteColorContainer;

    @Nullable
    public TextView text;

    @Nullable
    public TextView text2;

    @Nullable
    public TextView time;

    @Nullable
    public TextView title;

    public MaterialCardView listCard;

    public MediaEntryViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        text = itemView.findViewById(R.id.text);
        text2 = itemView.findViewById(R.id.text2);

        image = itemView.findViewById(R.id.image);
        time = itemView.findViewById(R.id.time);

        imageText = itemView.findViewById(R.id.imageText);
        imageTextContainer = itemView.findViewById(R.id.imageTextContainer);
        imageContainerCard = itemView.findViewById(R.id.imageContainerCard);
        imageContainer = itemView.findViewById(R.id.imageContainer);

        menu = itemView.findViewById(R.id.menu);
        imagePlaying = itemView.findViewById(R.id.imagePlaying);
        dragView = itemView.findViewById(R.id.drag_view);
        paletteColorContainer = itemView.findViewById(R.id.paletteColorContainer);
        mask = itemView.findViewById(R.id.mask);
        dummyContainer = itemView.findViewById(R.id.dummy_view);

        listCard = itemView.findViewById(R.id.listCard);

        if (imageContainerCard != null) {
            imageContainerCard.setCardBackgroundColor(Color.TRANSPARENT);
        }

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Nullable
    @Override
    public View getSwipeableContainerView() {
        return null;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public void setImageTransitionName(@NonNull String transitionName) {
        itemView.setTransitionName(transitionName);
    /* if (imageContainerCard != null) {
        imageContainerCard.setTransitionName(transitionName);
    }
    if (image != null) {
        image.setTransitionName(transitionName);
    }*/
    }
}
