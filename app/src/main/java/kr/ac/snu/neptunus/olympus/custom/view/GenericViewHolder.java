package kr.ac.snu.neptunus.olympus.custom.view;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by jjc93 on 2016-03-04.
 */
public class GenericViewHolder {
    private static String TAG = GenericViewHolder.class.getName();

    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
