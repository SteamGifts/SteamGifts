package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.mabako.steamgifts.fragments.interfaces.IHasPoll;

import org.jsoup.Connection;

public class EnterLeavePollTask extends AjaxTask<IHasPoll> {
    private static final String TAG = EnterLeavePollTask.class.getSimpleName();

    public static final String SELECT_ANSWER = "poll_vote_insert";
    public static final String REMOVE_ANSWER = "poll_vote_delete";

    private final int answerId;

    public EnterLeavePollTask(IHasPoll fragment, Context context, String xsrfToken, String what, int answerId) {
        super(fragment, context, xsrfToken, what);
        this.answerId = answerId;
    }

    @Override
    protected void addExtraParameters(Connection connection) {
        connection.data("poll_answer_id", String.valueOf(answerId));
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        super.onPostExecute(response);

        Log.d(TAG, "Response: " + response);
        if (response != null && response.statusCode() == 200) {
            Log.d(TAG, getWhat() + " ~> " + answerId);
            getFragment().onPollAnswerSelected(SELECT_ANSWER.equals(getWhat()) ? answerId : 0);
        } else {
            Toast.makeText(getContext(), "Unable to submit vote.", Toast.LENGTH_SHORT).show();
        }
    }
}
