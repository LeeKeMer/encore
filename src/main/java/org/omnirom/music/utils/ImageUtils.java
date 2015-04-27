/* * Copyright (C) 2014 Fastboot Mobile, LLC. * * This program is free software; you can redistribute it and/or modify it under the terms of the * GNU General Public License as published by the Free Software Foundation; either version 3 of * the License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See * the GNU General Public License for more details. * * You should have received a copy of the GNU General Public License along with this program; * if not, see <http://www.gnu.org/licenses>. */package org.omnirom.music.utils;import android.annotation.TargetApi;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.graphics.drawable.BitmapDrawable;import android.os.Build;import org.omnirom.music.art.ImageCache;/** * Image processing utility methods */public class ImageUtils {    /**     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)     * onward this returns the allocated memory size of the bitmap which can be larger than the     * actual bitmap data byte count (in the case it was re-used).     *     * @param value The bitmap to measure     * @return size in bytes     */    @TargetApi(Build.VERSION_CODES.KITKAT)    public static int getBitmapSize(BitmapDrawable value) {        Bitmap bitmap = value.getBitmap();        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be        // larger than bitmap byte count.        if (Utils.hasKitKat()) {            return bitmap.getAllocationByteCount();        }        return bitmap.getByteCount();    }    /**     * @param candidate - Bitmap to check     * @param targetOptions - Options that have the out* value populated     * @return true if <code>candidate</code> can be used for inBitmap re-use with     *      <code>targetOptions</code>     */    @TargetApi(Build.VERSION_CODES.KITKAT)    public static boolean canUseForInBitmap(            Bitmap candidate, BitmapFactory.Options targetOptions) {        if (!Utils.hasKitKat()) {            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1            return candidate.getWidth() == targetOptions.outWidth                    && candidate.getHeight() == targetOptions.outHeight                    && targetOptions.inSampleSize == 1;        }        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap        // is smaller than the reusable bitmap candidate allocation byte count.        int width = targetOptions.outWidth / targetOptions.inSampleSize;        int height = targetOptions.outHeight / targetOptions.inSampleSize;        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());        return byteCount <= candidate.getAllocationByteCount();    }    /**     * Return the byte usage per pixel of a bitmap based on its configuration.     * @param config The bitmap configuration.     * @return The byte usage per pixel.     */    private static int getBytesPerPixel(Bitmap.Config config) {        if (config == Bitmap.Config.ARGB_8888) {            return 4;        } else if (config == Bitmap.Config.RGB_565) {            return 2;        } else if (config == Bitmap.Config.ARGB_4444) {            return 2;        } else if (config == Bitmap.Config.ALPHA_8) {            return 1;        }        return 1;    }    public static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {        // inBitmap only works with mutable bitmaps so force the decoder to        // return mutable bitmaps.        options.inMutable = true;        if (options.inSampleSize == 0) {            // TODO: Better calculations            if (Utils.hasKitKat()) {                // We assume that KitKat devices are not-too-bad devices                options.inSampleSize = 2;            } else {                // Otherwise load low quality images on very old devices                options.inSampleSize = 4;            }        }        if (cache != null) {            // Try and find a bitmap to use for inBitmap            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);            if (inBitmap != null) {                options.inBitmap = inBitmap;            }        }    }}