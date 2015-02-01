package com.amaze.filemanager.adapters;

/**
 * Created by Arpit on 25-01-2015.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amaze.filemanager.R;
import com.amaze.filemanager.fragments.RarViewer;
import com.amaze.filemanager.fragments.ZipViewer;
import com.amaze.filemanager.services.asynctasks.RarExtractTask;
import com.amaze.filemanager.services.asynctasks.RarHelperTask;
import com.amaze.filemanager.services.asynctasks.ZipExtractTask;
import com.amaze.filemanager.services.asynctasks.ZipHelperTask;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.Icons;
import com.amaze.filemanager.utils.RoundedImageView;
import com.amaze.filemanager.utils.ZipObj;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RarAdapter extends ArrayAdapter<ZipObj> {
    Context c;
    Drawable folder, unknown;
    ArrayList<FileHeader> enter;
    RarViewer zipViewer;
    private SparseBooleanArray myChecked = new SparseBooleanArray();
    public RarAdapter(Context c, int id, ArrayList<FileHeader> enter, RarViewer zipViewer) {
        super(c, id);
        System.out.println(enter.size()+"");
        this.enter = enter;
        for (int i = 0; i < enter.size(); i++) {
            myChecked.put(i, false);
        }
        this.c = c;
        folder = c.getResources().getDrawable(R.drawable.ic_grid_folder_new);
        unknown = c.getResources().getDrawable(R.drawable.ic_doc_generic_am);
        this.zipViewer = zipViewer;
    }public void toggleChecked(int position) {
        if (myChecked.get(position)) {
            myChecked.put(position, false);
        } else {
            myChecked.put(position, true);
        }

        notifyDataSetChanged();
        if (zipViewer.selection == false || zipViewer.mActionMode == null) {
            zipViewer.selection = true;
            /*zipViewer.mActionMode = zipViewer.getActivity().startActionMode(
                   zipViewer.mActionModeCallback);*/
            zipViewer.mActionMode = zipViewer.mainActivity.toolbar.startActionMode(zipViewer.mActionModeCallback);
        }
        zipViewer.mActionMode.invalidate();
        if (getCheckedItemPositions().size() == 0) {
            zipViewer.selection = false;
            zipViewer.mActionMode.finish();
            zipViewer.mActionMode = null;
        }
    }

    public void toggleChecked(boolean b,String path) {
        int k=0;
       // if(enter.get(0).getEntry()==null)k=1;
        for (int i = k; i < enter.size(); i++) {
            myChecked.put(i, b);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getCheckedItemPositions() {
        ArrayList<Integer> checkedItemPositions = new ArrayList<Integer>();

        for (int i = 0; i < myChecked.size(); i++) {
            if (myChecked.get(i)) {
                (checkedItemPositions).add(i);
            }
        }

        return checkedItemPositions;
    }


    private class ViewHolder {
        RoundedImageView viewmageV;
        ImageView imageView,apk;
        TextView txtTitle;
        TextView txtDesc;
        TextView date;
        TextView perm;
        View rl;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final FileHeader rowItem = enter.get(position);
        View view = convertView;
        final int p = position;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) c
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.rowlayout, parent, false);
            final ViewHolder vholder = new ViewHolder();

            vholder.txtTitle = (TextView) view.findViewById(R.id.firstline);
            if (zipViewer.mainActivity.theme1==1)
                vholder.txtTitle.setTextColor(getContext().getResources().getColor(android.R.color.white));
            vholder.viewmageV = (RoundedImageView) view.findViewById(R.id.cicon);
            vholder.imageView = (ImageView) view.findViewById(R.id.icon);
            vholder.rl = view.findViewById(R.id.second);
            vholder.perm = (TextView) view.findViewById(R.id.permis);
            vholder.date = (TextView) view.findViewById(R.id.date);
            vholder.txtDesc = (TextView) view.findViewById(R.id.secondLine);
            vholder.apk=(ImageView)view.findViewById(R.id.bicon);
            view.setTag(vholder);

        }
        final ViewHolder holder = (ViewHolder) view.getTag();


        GradientDrawable gradientDrawable = (GradientDrawable) holder.imageView.getBackground();
        
            holder.imageView.setImageDrawable(Icons.loadMimeIcon(zipViewer.getActivity(), rowItem.getFileNameString(), false));
        holder.txtTitle.setText(rowItem.getFileNameString().substring(rowItem.getFileNameString().lastIndexOf("\\") + 1));
            if (rowItem.isDirectory()) {
                holder.imageView.setImageDrawable(folder);
                gradientDrawable.setColor(Color.parseColor(zipViewer.skin));} else {
                if (zipViewer.coloriseIcons) {
                    if (Icons.isVideo(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#f06292"));
                    else if (Icons.isAudio(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#9575cd"));
                    else if (Icons.isPdf(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#da4336"));
                    else if (Icons.isCode(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#00bfa5"));
                    else if (Icons.isText(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#e06055"));
                    else if (Icons.isArchive(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#f9a825"));
                    else if (Icons.isgeneric(rowItem.getFileNameString()))
                        gradientDrawable.setColor(Color.parseColor("#9e9e9e"));
                    else gradientDrawable.setColor(Color.parseColor(zipViewer.skin));
                } else gradientDrawable.setColor(Color.parseColor(zipViewer.skin));
            }
        

        holder.rl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toggleChecked(p);
                return false;
            }
        });holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   final Animation animation = AnimationUtils.loadAnimation(zipViewer.getActivity(), R.anim.holder_anim);

                    holder.imageView.setAnimation(animation);
                    toggleChecked(p);}

            }
        );
        Boolean checked = myChecked.get(position);
        if (checked != null) {

            if (checked) {
                holder.imageView.setImageDrawable(zipViewer.getResources().getDrawable(R.drawable.abc_ic_cab_done_holo_dark));
                gradientDrawable.setColor(Color.parseColor("#757575"));

                if (Build.VERSION.SDK_INT >= 21) {

                    if (zipViewer.mainActivity.theme1==1)
                        holder.rl.setBackgroundColor(getContext().getResources().getColor(android.R.color.black));
                    else
                        holder.rl.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
                    holder.rl.setElevation(10f);
                }
                else
                    holder.rl.setBackgroundColor(zipViewer.skinselection);
            } else {

                holder.rl.setBackgroundResource(R.drawable.listitem1);

            }
        }
        holder.rl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View p1) {
                    if(zipViewer.selection)toggleChecked(p);else {

                        if (rowItem.isDirectory()) {

                            new RarHelperTask(zipViewer,  rowItem.getFileNameString()).execute(zipViewer.f);

                        }else {
                            String x=rowItem.getFileNameString().substring(rowItem.getFileNameString().lastIndexOf("/")+1);
                            File file = new File(getContext().getCacheDir().getAbsolutePath() + "/" + x);
                            zipViewer.files.clear();
                            zipViewer.files.add(0, file);
                            try {
                                new RarExtractTask(new Archive(zipViewer.f), getContext().getCacheDir().getAbsolutePath(), zipViewer, x, true).execute(rowItem);
                            } catch (RarException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }}
        });


        return view;
    }

    @Override
    public int getCount() {
        return enter.size();
    }

    @Override
    public long getItemId(int arg0) {
        return enter.size();
    }
}
