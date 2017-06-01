package halamish.reem.remember.activity.main;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import halamish.reem.remember.R;

/**
 * Created by Re'em on 5/25/2017.
 */

public class ImageViewDialog extends Dialog {
    private Bitmap bitmap;
    private ImageView mainView;

    public ImageViewDialog(Context context) {
        super(context, R.style.ImageViewDialog);
        setContentView(R.layout.dialog_image_view);
        this.bitmap = bitmap;
        mainView = (ImageView) findViewById(R.id.dialog_image_view);
        mainView.setImageBitmap(bitmap);
    }

    public void show(Bitmap bitmap) {
        this.bitmap = bitmap;
        mainView.setImageBitmap(bitmap);
        super.show();
    }
}
