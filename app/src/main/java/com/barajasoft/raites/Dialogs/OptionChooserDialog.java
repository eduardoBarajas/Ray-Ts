package com.barajasoft.raites.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;

public class OptionChooserDialog extends Dialog {
    private String optionSelected = "None";
    public OptionChooserDialog(@NonNull Context context, String dlgTag, String titulo, String opc1, String opc2, ResultListener listener) {
        super(context);
        setContentView(R.layout.option_chooser_dialog);
        TextView title = findViewById(R.id.title);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);
        Button btnOpcionOne = findViewById(R.id.btnOpcionOne);
        Button btnOpcionTwo = findViewById(R.id.btnOpcionTwo);
        btnOpcionOne.setText(opc1);
        btnOpcionTwo.setText(opc2);
        btnOpcionOne.setOnClickListener(e->{
            btnOpcionOne.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnOpcionTwo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0277BD")));
            optionSelected = opc1;
            listener.result(dlgTag,optionSelected);
            dismiss();
        });
        btnOpcionTwo.setOnClickListener(e->{
            btnOpcionTwo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnOpcionOne.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0277BD")));
            optionSelected = opc2;
            listener.result(dlgTag,optionSelected);
            dismiss();
        });
        title.setText(titulo);
    }
}
