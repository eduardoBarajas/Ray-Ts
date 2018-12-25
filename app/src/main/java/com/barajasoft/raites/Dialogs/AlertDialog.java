package com.barajasoft.raites.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import com.barajasoft.raites.R;

public class AlertDialog extends Dialog {
    public AlertDialog(@NonNull Activity activity, String titulo, String mensaje) {
        super(activity);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.alert_dialog);
        Button btnOk = findViewById(R.id.alertButton);
        TextView title, message;
        title = findViewById(R.id.alertTitle);
        message = findViewById(R.id.alertMessage);
        title.setText(titulo);
        message.setText(mensaje);
        btnOk.setOnClickListener(e->{
            dismiss();
        });
    }
}
