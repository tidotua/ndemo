package com.tidotua.ndemo.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.tidotua.ndemo.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ti on 15.12.15.
 */
public class SpinnerAdapter extends SimpleAdapter {

    public final static String TITLE_FIELD = "title";
    public final static String IMAGE_FIELD = "image";

    private LayoutInflater mInflater;
    private List<? extends Map<String, ?>> mData;



    public static List<? extends Map<String, ?>> initSpinnerList(Context context, int[] names, int[] images) {
        if (names.length != images.length) {
            return null;
        }

        List<Map<String, Object>> spinnerList = new LinkedList<Map<String, Object>>();

        for (int i = 0; i < names.length; i++) {
            Map map = new HashMap<String, Object>();

            map.put(IMAGE_FIELD, images[i]);
            map.put(TITLE_FIELD, context.getString(names[i]));
            spinnerList.add(map);
        }
        return spinnerList;
    }

    public SpinnerAdapter(Context context, List<? extends Map<String, ?>> data,
                                int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mData =data;
        mInflater= LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.mode_spinner_row,
                    null);
        }
        ((TextView) convertView.findViewById(R.id.modeTitleTextView))
                .setText((String) mData.get(position).get(TITLE_FIELD));
        ((ImageView) convertView.findViewById(R.id.modeImageView))
                .setImageResource((Integer)mData.get(position).get(IMAGE_FIELD));
        return convertView;
    }
}
