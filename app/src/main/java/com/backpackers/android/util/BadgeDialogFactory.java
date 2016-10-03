package com.backpackers.android.util;

import com.bumptech.glide.Glide;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BadgeDialogFactory {

    public static Dialog createBadgeDialog(Context context, String badgeTitle, String badgeUrl,
                                           String content) {
        final View badgeView = View.inflate(context, com.backpackers.android.R.layout.dialog_badge, null);

        Glide.with(badgeView.getContext())
                .load(badgeUrl)
                .into((ImageView) badgeView.findViewById(com.backpackers.android.R.id.image_badge));

        TextView contentTv = (TextView) badgeView.findViewById(com.backpackers.android.R.id.text_badge_content);
        contentTv.setText(content);

        return new AlertDialog.Builder(context)
                .setView(badgeView)
                .setTitle(badgeTitle)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
    }

    public static Dialog createBadgeDialog(Context context, String badgeTitle, Drawable badge,
                                           String content, final DialogInterface dialogInterface) {
        final View badgeView = View.inflate(context, com.backpackers.android.R.layout.dialog_badge, null);

        ImageView badgeIv = (ImageView) badgeView.findViewById(com.backpackers.android.R.id.image_badge);
        badgeIv.setImageDrawable(badge);

        TextView contentTv = (TextView) badgeView.findViewById(com.backpackers.android.R.id.text_badge_content);
        contentTv.setText(content);

        return new AlertDialog.Builder(context)
                .setView(badgeView)
                .setTitle(badgeTitle)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .create();
    }
}
