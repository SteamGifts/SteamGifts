package net.mabako.steamgifts.adapters.viewholder;

import android.text.Html;
import android.text.Spanned;

public final class Utils {
    public static CharSequence fromHtml(String source) {
        try {
            CharSequence cs = Html.fromHtml(source, null, new CustomHtmlTagHandler());
            cs = cs.subSequence(0, cs.length() - 2);
            return cs;

        } catch (Exception e) {
            CharSequence cs = Html.fromHtml(source);
            cs = cs.subSequence(0, cs.length() - 2);
            return cs;
        }
    }
}
