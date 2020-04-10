package com.esenlermotionstar.nogate.MainActivityHelpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.esenlermotionstar.nogate.R;

public class ApprovedContentItemHelper {
    static double scale = 1;
    public static ItemTouchHelper.Callback getItemTouchHelperForApprovedContent(final Context applicationContext, TouchHelperRemoveEvent removeItemEvent)
    {
        final TouchHelperRemoveEvent RemoveEvent = removeItemEvent;
        final Context ApplicationContext = applicationContext;
        ItemTouchHelper.Callback simpTchHelp = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

                return true;
            }

            Drawable defaultBck;

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    final ColorDrawable background_delete = new ColorDrawable(Color.rgb(244,67,54));
                    //final ColorDrawable background_select = new ColorDrawable(Color.rgb(232,147,44));
                    View itemView = viewHolder.itemView;
                    Drawable icon;

                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        icon =  ResourcesCompat.get
                    } else {
                        icon =  ResourcesCompat.getDrawable(id);
                    }*/
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
                    {
                       icon = ContextCompat.getDrawable(ApplicationContext, R.drawable.ic_remove_png);

                    }
                    else{
                        icon = ContextCompat.getDrawable(ApplicationContext, R.drawable.ic_action_remove);
                        //Drawable selectIc = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_check_box_white_24dp);
                        String color = "#FFFFFF";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            icon.setTint(Color.parseColor(color));
                        }
                        else
                        {
                            icon.mutate().setColorFilter(0xffffff, PorterDuff.Mode.SRC_IN);
                        }
                    }


                    scale = applicationContext.getResources().getDisplayMetrics().density;
                    if (dX > 0) {
                        //SOLDAN SAĞA - silme
                        background_delete.setBounds(itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getLeft() + (int) dX, viewHolder.itemView.getBottom());

                        int icLeft = (int)(itemView.getLeft() + 16  * scale),
                                icRight = (int)(icLeft + 32 * scale),
                                icTop = (int)(itemView.getTop() + itemView.getHeight() / 2 - 16  * scale),
                                icBot = (int)(icTop + 32 * scale);
                        icon.setBounds(icLeft, icTop, icRight, icBot);

                    } else {
                        //SAĞDAN SOLA - seçme
                        background_delete.setBounds(itemView.getRight() + (int) dX, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());

                        int icLeft = (int)((itemView.getRight()) - 48 * scale),
                                icRight = (int)(icLeft + 32 * scale),
                                icTop = (int)(itemView.getTop() + itemView.getHeight() / 2 - 16 * scale),
                                icBot = (int)(icTop + 32 * scale);
                        icon.setBounds(icLeft, icTop, icRight, icBot);


                    }
                    background_delete.draw(c);
                    icon.draw(c);
                    // compute top and left margin to the view bounds



                }


            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                RemoveEvent.RemoveItemByPosition(viewHolder.getAdapterPosition());

            }
        };
        return simpTchHelp;
    }

    public  static abstract class TouchHelperRemoveEvent
    {
        public abstract void RemoveItemByPosition(int i);
    }
}
