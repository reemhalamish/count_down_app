package halamish.reem.remember.activity.main.recycler_view;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import static android.support.v7.widget.helper.ItemTouchHelper.END;
import static android.support.v7.widget.helper.ItemTouchHelper.START;

/**
 * Created by Re'em on 6/11/2017.
 */

public class RowMovingTouchListener extends ItemTouchHelper.SimpleCallback  {
    private final ElementSwiped callbacks;

    public interface ElementSwiped {
        void onSwipe(RecyclerView.ViewHolder viewHolder);
    }


    private static final String TAG = RowMovingTouchListener.class.getCanonicalName();

    /**
     * Creates a Callback for the given drag and swipe allowance. These values serve as
     * defaults
     * and if you want to customize behavior per ViewHolder, you can override
     * {@link #getSwipeDirs(RecyclerView, ViewHolder)}
     * and / or {@link #getDragDirs(RecyclerView, ViewHolder)}.
     *
     * @param dragDirs  Binary OR of direction flags in which the Views can be dragged. Must be
     *                  composed of {@link #LEFT}, {@link #RIGHT}, {@link #START}, {@link
     *                  #END},
     *                  {@link #UP} and {@link #DOWN}.
     * @param swipeDirs Binary OR of direction flags in which the Views can be swiped. Must be
     *                  composed of {@link #LEFT}, {@link #RIGHT}, {@link #START}, {@link
     *                  #END},
     *                  {@link #UP} and {@link #DOWN}.
     */
    public RowMovingTouchListener(ElementSwiped callbacks) {
        super(0, START | END);
//        int dragDirs = 0;
//        int swipeDirs = START;

        this.callbacks = callbacks;
    }

//    /**
//     * to enable swipe from any point in the view
//     * @return
//     */
//    @Override
//    public boolean isItemViewSwipeEnabled() {
//        return true;
//    }

    /**
     * Called when ItemTouchHelper wants to move the dragged item from its old position to
     * the new position.
     * <p>
     * If this method returns true, ItemTouchHelper assumes {@code viewHolder} has been moved
     * to the adapter position of {@code target} ViewHolder
     * ({@link ViewHolder#getAdapterPosition()
     * ViewHolder#getAdapterPosition()}).
     * <p>
     * If you don't support drag & drop, this method will never be called.
     *
     * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
     * @param viewHolder   The ViewHolder which is being dragged by the user.
     * @param target       The ViewHolder over which the currently active item is being
     *                     dragged.
     * @return True if the {@code viewHolder} has been moved to the adapter position of
     * {@code target}.
     * @see #onMoved(RecyclerView, ViewHolder, int, ViewHolder, int, int, int)
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    /**
     * Called when a ViewHolder is swiped by the user.
     * <p>
     * If you are returning relative directions ({@link #START} , {@link #END}) from the
     * {@link #getMovementFlags(RecyclerView, ViewHolder)} method, this method
     * will also use relative directions. Otherwise, it will use absolute directions.
     * <p>
     * If you don't support swiping, this method will never be called.
     * <p>
     * ItemTouchHelper will keep a reference to the View until it is detached from
     * RecyclerView.
     * As soon as it is detached, ItemTouchHelper will call
     * {@link #clearView(RecyclerView, ViewHolder)}.
     *
     * @param viewHolder The ViewHolder which has been swiped by the user.
     * @param direction  The direction to which the ViewHolder is swiped. It is one of
     *                   {@link #UP}, {@link #DOWN},
     *                   {@link #LEFT} or {@link #RIGHT}. If your
     *                   {@link #getMovementFlags(RecyclerView, ViewHolder)}
     *                   method
     *                   returned relative flags instead of {@link #LEFT} / {@link #RIGHT};
     *                   `direction` will be relative as well. ({@link #START} or {@link
     *                   #END}).
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(TAG, "swiped! " + viewHolder.getAdapterPosition());
        callbacks.onSwipe(viewHolder);
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getAdapterPosition() == -1) return 0;
        if (viewHolder.getAdapterPosition() == ((EventAdapter)recyclerView.getAdapter()).getPositionForNow()) return 0;
        return super.getSwipeDirs(recyclerView, viewHolder);
    }


}
