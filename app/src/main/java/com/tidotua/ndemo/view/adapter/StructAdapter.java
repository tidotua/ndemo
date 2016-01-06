package com.tidotua.ndemo.view.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nestapi.lib.API.Structure;
import com.tidotua.ndemo.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ti on 13.12.15.
 */
public class StructAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Structure> mItems = new ArrayList<Structure>();
    private View.OnClickListener onClickListner;
    PorterDuffColorFilter activeColorFilter;
    PorterDuffColorFilter inactiveColorFilter;

    public StructAdapter(Context context, View.OnClickListener onClickListner) {
        mContext = context;
        this.onClickListner = onClickListner;
        activeColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.nest_blue),
                PorterDuff.Mode.SRC_IN);
        inactiveColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.nest_gray),
                PorterDuff.Mode.SRC_IN);
    }

    public void reset() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void setItems(Collection<Structure> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mItems.size()) {
            return mItems.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup itemView = (ViewGroup)convertView;
        if (null == itemView) {
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (ViewGroup)layoutInflater.inflate(R.layout.structure_item, null);
        }
        itemView.setOnClickListener(onClickListner);
        itemView.setTag(mItems.get(position).getStructureID());
        setContentToView(itemView, position);
        return itemView;
    }

    private void setContentToView(ViewGroup itemViewGroup, int position)
    {
        TextView titleText = (TextView)itemViewGroup.findViewById(R.id.structTextView);
        TextView descText = (TextView)itemViewGroup.findViewById(R.id.structDescTextView);
        ImageView homeImage = (ImageView)itemViewGroup.findViewById(R.id.structImageView);
        titleText.setText("");
        if (null != mItems) {
            Structure structure = mItems.get(position);
            if (null != structure) {
                titleText.setText(structure.getName());
                boolean isHome = structure.getAwayState() == Structure.AwayState.HOME;
                homeImage.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_home));
                homeImage.setColorFilter(isHome ? activeColorFilter : inactiveColorFilter);
                descText.setText(mContext.getString(isHome ? R.string.home : R.string.away));
            }
        }
    }
}
