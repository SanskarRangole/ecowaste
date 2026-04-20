package com.example.ecowaste.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecowaste.R;
import com.example.ecowaste.adapters.GalleryAdapter;
import com.example.ecowaste.models.GalleryImage;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<GalleryImage> imageList = new ArrayList<>();
        imageList.add(new GalleryImage(R.drawable.drive, "Beauty created through reuse and recycling"));
        imageList.add(new GalleryImage(R.drawable.cd, "Shiny disco ball created using pieces of old CDs"));
        imageList.add(new GalleryImage(R.drawable.tree, "Safe Battery Disposal"));
        imageList.add(new GalleryImage(R.drawable.girll, "An artistic piece created from old light bulbs"));
        imageList.add(new GalleryImage(R.drawable.bulb, "Decorative oil lamps crafted from old glass light bulbs."));
        imageList.add(new GalleryImage(R.drawable.logo, "ReCycleIT Initiative"));
        imageList.add(new GalleryImage(R.drawable.art, "Creative artwork made from recycled electronic circuit boards"));
        imageList.add(new GalleryImage(R.drawable.kk, "Where waste finds a beautiful new life"));
        imageList.add(new GalleryImage(R.drawable.bike, "Trash transformed into timeless creativity"));
        imageList.add(new GalleryImage(R.drawable.heart, "Beauty born from what was once discarded"));
        imageList.add(new GalleryImage(R.drawable.fourwheel, "Transforming waste into art and purpose"));
        imageList.add(new GalleryImage(R.drawable.pa, "Sustainability made beautiful"));
        imageList.add(new GalleryImage(R.drawable.rr, "Reuse today for a better tomorrow"));
        imageList.add(new GalleryImage(R.drawable.robot, "Old materials, new magic"));


        imageList.add(new GalleryImage(R.drawable.holder, "Useful desk organizer made from recycled keyboard keys."));
        imageList.add(new GalleryImage(R.drawable.slipper, "Waste can be transformed into beauty"));

        GalleryAdapter adapter = new GalleryAdapter(this, imageList);
        recyclerView.setAdapter(adapter);
    }
}
