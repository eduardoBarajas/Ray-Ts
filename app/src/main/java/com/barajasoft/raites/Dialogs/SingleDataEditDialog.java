package com.barajasoft.raites.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.TextView;

import com.barajasoft.raites.R;

public class SingleDataEditDialog extends Dialog {
    public SingleDataEditDialog(@NonNull Activity activity) {
        super(activity);
        setContentView(R.layout.single_data_edit_dialog);
        TextView title = findViewById(R.id.title);
        TextInputEditText dato = findViewById(R.id.textEditDialog);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(e->{

        });
    }
}
