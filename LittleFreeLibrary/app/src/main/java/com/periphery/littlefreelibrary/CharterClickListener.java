package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.view.View;

public class CharterClickListener implements View.OnClickListener {
    private Charter charter;

    public CharterClickListener(Charter charter) {
        this.charter = charter;
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(v.getContext(), SpecificCharter.class);
        intent.putExtra("charter", this.charter);
        v.getContext().startActivity(intent);
    }
}

