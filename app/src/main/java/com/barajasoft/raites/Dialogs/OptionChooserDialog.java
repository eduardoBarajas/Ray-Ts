package com.barajasoft.raites.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barajasoft.raites.R;

public class OptionChooserDialog extends Dialog {
    public OptionChooserDialog(@NonNull Activity activity, String titulo, String[] opciones) {
        super(activity);
        setContentView(R.layout.option_chooser_dialog);
        TextView title = findViewById(R.id.title);
        LinearLayout layout = findViewById(R.id.layout);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);
        title.setText(titulo);
        for(String opc : opciones){
            Button btn = new Button(getContext());
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.weight = 1;
            btnParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            btnParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            btn.setBackgroundColor(Color.parseColor("#0277BD"));
            btn.setTextColor(Color.parseColor("#EEEEEE"));
            btn.setText(opc);
            //falta el font family
            btn.setLayoutParams(btnParams);
            layout.addView(btn);
        }
        btnConfirmar.setOnClickListener(e->{

        });
    }
}
