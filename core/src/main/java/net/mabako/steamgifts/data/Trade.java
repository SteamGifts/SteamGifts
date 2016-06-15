package net.mabako.steamgifts.data;

import android.content.Context;
import android.support.annotation.LayoutRes;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;
import java.util.Calendar;

/**
 * A Trade.
 */
public class Trade extends BasicTrade implements Serializable, IEndlessAdaptable {
    private static final long serialVersionUID = -4828592066348698719L;

    private String title;
    private String name;
    private String creator;
    private CustomDateTime createdTime;
    private String creatorAvatar;

    /**
     * Since all threads and posts within the subsection contain a +/- score of each user, show
     * this here.
     */
    private int creatorScorePositive, creatorScoreNegative;

    private boolean locked;

    public Trade(String tradeId) {
        super(tradeId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Calendar getCreatedTime() {
        return createdTime != null ? createdTime.getCalendar() : null;
    }

    public String getRelativeCreatedTime(Context context) {
        return createdTime != null ? createdTime.toString(context) : null;
    }

    public void setCreatedTime(String time) {
        createdTime = new CustomDateTime(time, false);
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public void setCreatorAvatar(String creatorAvatar) {
        this.creatorAvatar = creatorAvatar;
    }

    public int getCreatorScorePositive() {
        return creatorScorePositive;
    }

    public void setCreatorScorePositive(int creatorScorePositive) {
        this.creatorScorePositive = creatorScorePositive;
    }

    public int getCreatorScoreNegative() {
        return creatorScoreNegative;
    }

    public void setCreatorScoreNegative(int creatorScoreNegative) {
        this.creatorScoreNegative = creatorScoreNegative;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    @LayoutRes
    public int getLayout() {
        return R.layout.trade_item;
    }
}
