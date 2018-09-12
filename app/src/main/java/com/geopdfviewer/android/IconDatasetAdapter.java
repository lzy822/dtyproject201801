package com.geopdfviewer.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.litepal.LitePal;

import java.io.File;
import java.net.URI;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;


/**
 * Created by 54286 on 2018/3/6.
 */

public class IconDatasetAdapter extends RecyclerView.Adapter<IconDatasetAdapter.ViewHolder> {
    private Context mContext;

    private List<IconDataset> iconDatasets;

    private OnRecyclerItemClickListener mOnItemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        ImageView Icon;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            Icon = (ImageView) view.findViewById(R.id.iconChoose);



        }
    }
    public IconDatasetAdapter(List<IconDataset> iconDatasets) {
        this.iconDatasets = iconDatasets;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.icon_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                IconDataset iconDataset = iconDatasets.get(position);
                mOnItemClick.onItemClick(v, iconDataset.getPath(), position);
                /*
                Intent intent = new Intent(mContext, MainInterface.class);
                intent.putExtra("num", map.getM_num());
                mContext.startActivity(intent);*/
            }
        });
        /*holder.xzqIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                xzq xzq = xzqs.get(position);
                Log.w(TAG, "onClick: " + xzq.getXzqmc());
                SharedPreferences pref1 = mContext.getSharedPreferences("xzq", MODE_PRIVATE);
                xzqs = DataUtil.bubbleSort(LitePal.where("grade = ?", Integer.toString(1)).find(xzq.class));
                if (pref1.getString("name", "").equals(xzq.getXzqmc())){
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("xzq", MODE_PRIVATE).edit();
                    editor.putString("name", "");
                    editor.apply();
                }else {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("xzq", MODE_PRIVATE).edit();
                    editor.putString("name", xzq.getXzqmc());
                    editor.apply();
                    xzqs.addAll(LitePal.where("sjxzq = ? and grade = ?", xzq.getXzqmc(), Integer.toString(2)).find(xzq.class));
                    xzqs = DataUtil.bubbleSort(xzqs);
                }
                //notifyItemChanged(position);
                //notifyItemRangeChanged(0, xzqs.size());
                notifyDataSetChanged();
            }
        });*/
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        //notifyItemChanged(position);
        if (payloads.isEmpty()){
            onBindViewHolder(holder, position);
        }else {
            notifyItemChanged(position);
            //Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();
            Log.w(TAG, "find here" + payloads.toString() );
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IconDataset iconDataset = iconDatasets.get(position);
        File file = new File(iconDataset.getPath());
        if (file.exists()) {
            Bitmap bitmap = DataUtil.getImageThumbnail(iconDataset.getPath(), 200, 200);
            //holder.Icon.setImageURI(Uri.fromFile(file));
            holder.Icon.setImageBitmap(bitmap);
        }else{
            holder.cardView.setCardBackgroundColor(Color.BLACK);
            holder.Icon.setImageResource(R.drawable.imgerror);
        }
    }

    @Override
    public int getItemCount() {
        return iconDatasets.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view, String xzqdm, int position);
    }

    public interface OnRecyclerItemClickListener{
        void onItemClick(View view, String iconPath, final int position);
    }
    public void setOnItemClickListener(OnRecyclerItemClickListener listener){
        this.mOnItemClick =  listener;
    }
}
