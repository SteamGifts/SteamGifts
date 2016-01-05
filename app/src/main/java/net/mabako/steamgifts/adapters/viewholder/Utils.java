package net.mabako.steamgifts.adapters.viewholder;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public final class Utils {
    public static CharSequence fromHtml(String source) {
        try {
            CharSequence cs = Html.fromHtml(source, null, new CustomHtmlTagHandler());
            cs = cs.subSequence(0, cs.length() - 2);
            return cs;
        } catch (Exception e) {
            Log.e(Utils.class.getSimpleName(), "Failed to parse HTML with custom parser", e);

            CharSequence cs = Html.fromHtml(source);
            cs = cs.subSequence(0, cs.length() - 2);
            return cs;
        }
    }
}
