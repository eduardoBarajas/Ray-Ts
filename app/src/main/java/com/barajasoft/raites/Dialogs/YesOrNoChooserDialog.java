package com.barajasoft.raites.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.widget.TextView;

import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;

public class YesOrNoChooserDialog extends Dialog {
    public YesOrNoChooserDialog(@NonNull Context context, String tag, String titulo, String cuerpo, ResultListener listener) {
        super(context);
        setContentView(R.layout.yes_or_no_dialog);
        setCanceledOnTouchOutside(false);
        TextView header = findViewById(R.id.HeaderText);
        TextView body = findViewById(R.id.bodyText);
        FloatingActionButton accept = findViewById(R.id.AcceptButton);
        FloatingActionButton cancel = findViewById(R.id.CancelButton);
        header.setText(titulo);
        body.setText(cuerpo);
        accept.setOnClickListener(e->{
            listener.result(tag, "Accept");
            dismiss();
        });
        cancel.setOnClickListener(e->{
            listener.result(tag, "Cancel");
            dismiss();
        });
    }
}
