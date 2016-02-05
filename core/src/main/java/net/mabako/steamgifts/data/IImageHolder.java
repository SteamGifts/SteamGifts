package net.mabako.steamgifts.data;

import java.util.List;

/**
 * Giveaway, Discussion or Comment that can have an image attached.
 */
public interface IImageHolder {
    /**
     * Attach an image to this instance.
     *
     * @param imageUrl url of the image
     */
    void attachImage(String imageUrl);

    /**
     * Returns all attached images.
     *
     * @return List of all attached images, or null if none was attached.
     */
    List<String> getAttachedImages();
}
