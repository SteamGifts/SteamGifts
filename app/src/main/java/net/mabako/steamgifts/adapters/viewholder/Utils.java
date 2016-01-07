package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import net.mabako.steamgifts.R;

public final class Utils {
    public static CharSequence fromHtml(String source) {
        return fromHtml(source, true);
    }

    public static CharSequence fromHtml(String source, boolean useCustomViewHandler) {
        if (TextUtils.isEmpty(source))
            return source;

        if (useCustomViewHandler) {
            try {
                CharSequence cs = Html.fromHtml(source, null, new CustomHtmlTagHandler());
                cs = cs.subSequence(0, cs.length() - 2);
                return cs;
            } catch (Exception e) {
                Log.e(Utils.class.getSimpleName(), "Failed to parse HTML with custom parser", e);
            }
        }

        CharSequence cs = Html.fromHtml(source);
        cs = cs.subSequence(0, cs.length() - 2);
        return cs;
    }

    public static void setBackgroundDrawable(Context context, View view, boolean highlighted) {
        if (highlighted) {
            int attrs[] = new int[]{R.attr.colorHighlightBackground};
            TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(ta.getDrawable(0));
            } else {
                view.setBackground(ta.getDrawable(0));
            }
        } else {
            view.setBackgroundResource(R.color.colorTransparent);
        }
    }
}
