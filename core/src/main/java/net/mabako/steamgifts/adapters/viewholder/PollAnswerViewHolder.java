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
    private final ProgressBar progressBar;
    private final TextView percentageText;

    private final Button button;

    /**
     * Spacer to replace the {@link #button}'s margin + padding.
     */
    private final View buttonSpace;

    public PollAnswerViewHolder(View itemView) {
        super(itemView);

        text = (TextView) itemView.findViewById(R.id.text);
        button = (Button) itemView.findViewById(R.id.vote);
        buttonSpace = itemView.findViewById(R.id.space_for_no_voting);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        percentageText = (TextView) itemView.findViewById(R.id.percentage);
    }

    public void setFrom(Poll.Answer answer) {
        text.setText(answer.getText());
        updateButtonText(answer.isSelected(), answer.isVoteable());

        Poll poll = answer.getPoll();
        int percentage = poll.getTotalVotes() == 0 ? 0 : Math.round(100f * answer.getVoteCount() / poll.getTotalVotes());
        progressBar.setProgress(percentage);
        percentageText.setText(String.format("%d%%", percentage));
    }

    private void updateButtonText(boolean votedThis, boolean canVote) {
        if (!canVote) {
            button.setVisibility(View.GONE);
            buttonSpace.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
            button.setText(votedThis ? "{faw-check-circle}" : "{faw-circle-o}");
            buttonSpace.setVisibility(View.GONE);
        }
    }
}
