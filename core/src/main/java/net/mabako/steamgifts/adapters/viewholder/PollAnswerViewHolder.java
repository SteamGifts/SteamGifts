package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Poll;

public class PollAnswerViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;
    private final Button button;
    private final ProgressBar progressBar;

    public PollAnswerViewHolder(View itemView) {
        super(itemView);

        text = (TextView) itemView.findViewById(R.id.text);
        button = (Button) itemView.findViewById(R.id.vote);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
    }

    public void setFrom(Poll.Answer answer) {
        text.setText(answer.getText());
        updateButtonText(false, true);

        Poll poll = answer.getPoll();
        progressBar.setProgress(100 * answer.getVoteCount() / poll.getTotalVotes());
    }

    private void updateButtonText(boolean votedThis, boolean canVote) {
        if (!canVote)
            button.setVisibility(View.GONE);
        else
            button.setText(votedThis ? "{faw-check}" : "{faw-arrow-right}");
    }
}
