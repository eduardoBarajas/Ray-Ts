package com.barajasoft.raites.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;

public class TimePickerDialog extends Dialog {
    private int hora, minuto;
    public TimePickerDialog(@NonNull Context context, ResultListener listener) {
        super(context);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.time_picker_dialog);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);
        TimePicker timePicker = findViewById(R.id.hora);
        ResultListener innerListener = new ResultListener() {
            @Override
            public void result(String tag, Object result) {
                if(tag.equals("innerDialog")&&((String)result).equals("Accept")){
                    listener.result("TimeSelected", String.valueOf(hora)+":"+String.valueOf(minuto));
                    dismiss();
                }
            }
        };
        btnConfirmar.setOnClickListener(e->{
            if(Build.VERSION.SDK_INT < 23){
                hora = timePicker.getCurrentHour();
                minuto = timePicker.getCurrentMinute();
            } else{
                hora = timePicker.getHour();
                minuto = timePicker.getMinute();
            }
            YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(context, "innerDialog", "Confirmar hora", "Seguro que quieres" +
                    " usar esta hora? \n"+hora+":"+minuto, innerListener);
            dlg.show();
        });
    }
}
