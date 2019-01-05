package com.barajasoft.raites.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.DatePicker;

import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;

public class DatePickerDialog extends Dialog {
    private int dia, anio, mes;
    public DatePickerDialog(@NonNull Context context, ResultListener listener) {
        super(context);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.date_picker_dialog);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);
        DatePicker datePicker = findViewById(R.id.fecha);
        ResultListener innerListener = new ResultListener() {
            @Override
            public void result(String tag, Object result) {
                if(tag.equals("innerDialog")&&((String)result).equals("Accept")){
                    listener.result("DateSelected", String.valueOf(dia)+"/"+String.valueOf(mes)+"/"+String.valueOf(anio));
                    dismiss();
                }
            }
        };
        btnConfirmar.setOnClickListener(e->{
            dia = datePicker.getDayOfMonth();
            mes = datePicker.getMonth() + 1;
            anio = datePicker.getYear();
            YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(context, "innerDialog", "Confirmar fecha", "Seguro que quieres" +
                    " usar esta fecha? \n"+dia+"/"+mes+ "/"+anio, innerListener);
            dlg.show();
        });
    }
}
