package com.barajasoft.raites.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Listeners.DialogResultListener;
import com.barajasoft.raites.R;

public class SingleDataEditDialog extends Dialog {
    public SingleDataEditDialog(@NonNull Activity activity, String TAG, String titulo, String currentData, DialogResultListener listener) {
        super(activity);
        setContentView(R.layout.single_data_edit_dialog);
        TextView title = findViewById(R.id.title);
        TextInputEditText dato = findViewById(R.id.textEditDialog);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);
        title.setText(titulo);
        dato.setText(currentData);
        DialogResultListener innerListener = new DialogResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                if(dlgTag.equals("SioNoDialog")){
                    if(((String)result).equals("Si")){
                        listener.result(TAG, dato.getText().toString());
                    }
                    dismiss();
                }
            }
        };
        btnConfirmar.setOnClickListener(e->{
            OptionChooserDialog dlg = new OptionChooserDialog(activity,"SioNoDialog","Guardar cambios?","Si","No",innerListener);
            dlg.show();
        });
    }
}
