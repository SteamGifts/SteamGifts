package net.mabako.steamgifts.data;

import java.util.List;

/**
 * Giveaway, Discussion or Comment that can have an image attached.
 */
public interface IImageHolder {
    /**
     * Attach an image to this instance.
     */
    void attachImage(Image image);

    /**
     * Returns all attached images.
     *
     * @return List of all attached images, or null if none was attached.
     */
    List<Image> getAttachedImages();
}
