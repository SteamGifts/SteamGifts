package net.mabako.steamgifts.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.mabako.steamgifts.PeriodicTasks;

/**
 * Upon device boot, this'll schedule all tasks for later execution.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PeriodicTasks.scheduleAllTasks(context);
    }
}
