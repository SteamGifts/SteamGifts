package net.mabako.steamgifts.fragments.interfaces;

/**
 * Notifications for entering or leaving a giveaway.
 */
public interface IHasEnterableGiveaways {
    /**
     * Enter or leave a giveaway upon the user's interaction.
     *
     * @param giveawayId ID of the giveaway
     * @param what       what kind of action to execute
     * @param xsrfToken  xsrf token
     */
    void requestEnterLeave(String giveawayId, String what, String xsrfToken);

    /**
     * Callback for a giveaway's status being updated.
     *
     * @param giveawayId ID of the giveaway
     * @param what       what kind of action was executed
     * @param success    whether or not the action was successful
     */
    void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate);
}
