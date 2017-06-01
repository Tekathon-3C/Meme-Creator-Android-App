package a3c.tekathon.memecreator;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.Activity;
import android.net.Uri;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageDisplay);
    }

    private static final int READ_REQUEST_CODE = 42;
    public void chooseFile(View view){

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);

    }

    public Uri imageUri;
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            imageUri = resultData.getData();

            imageView.setImageURI(imageUri);
        }
    }

    public void save(View view){
        Bitmap original = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
    //    System.out.println("didn't crash");
        Bitmap original2 = original.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(original2);
        EditText topText = (EditText) findViewById(R.id.topQuote);
        canvas.drawText(topText.getText().toString(), 0, 0, new Paint());

        EditText bottomText = (EditText) findViewById(R.id.bottomQuote);
        canvas.drawText(topText.getText().toString(), canvas.getWidth()-100, canvas.getHeight()-100, new Paint());

        view.draw(canvas);
        System.out.println(imageUri.getPath());
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/meme" + System.nanoTime() + ".jpg";
        File file = new File(directory);
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getParentFile().getAbsolutePath());
        System.out.println(file.getParentFile().list());
        System.out.println(file.getParentFile().exists());
        try {
            if (file.exists()) {
                file.delete();
            }
            if(file.createNewFile()){
                System.out.println("success");
            }

            OutputStream save = new FileOutputStream(directory);
            original2.compress(Bitmap.CompressFormat.JPEG, 100, save);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void upload(View view){
        System.out.println("Attempting Upload");
        try {
            SimpleFTP ftp = new SimpleFTP();

            // Connect to an FTP server on port 21.
            ftp.connect("192.157.192.252", 21, "tek1", "326788");

            // Set binary mode.
            ftp.bin();

            // Change to a new working directory on the FTP server.
            ftp.cwd("/public_html/");

            // Upload some files.
            File directory = new File(Environment.DIRECTORY_PICTURES);

            for(File file : directory.listFiles()){
                ftp.stor(file);
            }

            // Quit from the FTP server.
            ftp.disconnect();
        }
        catch (IOException e) {
            // Jibble.
        }
    }

    public Bitmap drawTextToBitmap(Context gContext,
                                   int gResId,
                                   String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                BitmapFactory.decodeResource(resources, gResId);

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one

        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}
