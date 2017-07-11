/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2video;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.ViewGroup;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitTextureView extends TextureView {

    private int mCameraWidth = 0;
    private int mCameraHeight = 0;
    private boolean mSquarePreview = false;
    private static final double ASPECT_RATIO = 4.0 / 4.0;
    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(int width, int height, boolean squarePreview) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mCameraWidth = width;
        mCameraHeight = height;
        mSquarePreview = squarePreview;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        final boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            if (width > height * ASPECT_RATIO) {
                width = (int) (height * ASPECT_RATIO + 0.5);
            } else {
                height = (int) (width / ASPECT_RATIO + 0.5);
            }
        } else {
            if (height > width * ASPECT_RATIO) {
                height = (int) (width * ASPECT_RATIO + 0.5);
            } else {
                width = (int) (height / ASPECT_RATIO + 0.5);
            }
        }

        setMeasuredDimension(width, height);
    }

    private Matrix setupTransform(int sw, int sh, int dw, int dh) {
        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, sw, sh);
        RectF dst = new RectF(0, 0, dw, dh);
        RectF screen = new RectF(0, 0, dw, dh);

        matrix.postRotate(-90, screen.centerX(), screen.centerY());
        matrix.mapRect(dst);

        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(src);

        matrix.setRectToRect(screen, src, Matrix.ScaleToFit.FILL);
        matrix.postRotate(-90, screen.centerX(), screen.centerY());

        return matrix;
    }

    private Matrix squareTransform(int viewWidth, int viewHeight) {
        Matrix matrix = new Matrix();

        if (viewWidth < viewHeight) {
           // MyLogger.log(AutoFitTextureView.class, "Horizontal");
            matrix.setScale(1, (float) mCameraHeight / (float) mCameraWidth, viewWidth / 2, viewHeight / 2);
        } else {
           // MyLogger.log(AutoFitTextureView.class, "Vertical");
            matrix.setScale((float) mCameraHeight / (float) mCameraWidth, 1, viewWidth / 2, viewHeight / 2);
        }

        return matrix;
    }
}