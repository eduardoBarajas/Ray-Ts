package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.barajasoft.raites.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends BaseActivity {
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private String pass, user;
    private final int SIGN_IN = 751;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.clientOath))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,gso);
        disableBottomMenu();
        disableDrawer();
        disableViewPager();
        setToolbar("#0277BD","Login");
        View layout = LayoutInflater.from(this).inflate(R.layout.login_activity,null);
        user = ((TextInputEditText)layout.findViewById(R.id.txtEmail)).getText().toString();
        pass = ((TextInputEditText)layout.findViewById(R.id.txtPass)).getText().toString();
        Button btnInicioSesion = layout.findViewById(R.id.btnInicioSesion);
        Button btnInicioSesionGoogle = layout.findViewById(R.id.btnInicioGoogle);
        Button btnCrearCuenta = layout.findViewById(R.id.btnCrearCuenta);
        btnCrearCuenta.setOnClickListener(e->{
            startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
        });
        if(auth.getCurrentUser()==null){
            googleSignInClient.signOut();
        }else{
            btnInicioSesionGoogle.setText("Continuar como " + auth.getCurrentUser().getDisplayName());
        }

        btnInicioSesionGoogle.setOnClickListener(e->{
            if(auth.getCurrentUser()!=null){
                Toast.makeText(getApplicationContext(),"Sesion iniciada con : \n"+auth.getCurrentUser().getDisplayName(),Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
                finish();
            }else{
                signIn();
            }
        });
        btnInicioSesion.setOnClickListener(e->{
            auth.signOut();
            googleSignInClient.signOut();
            signIn(user, pass);
        });
        addContent(layout);
    }

    private void signIn(String user, String pass) {
        if(!user.isEmpty() && !pass.isEmpty()){
            auth.signInWithEmailAndPassword(user, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                final FirebaseUser user = auth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Sesion iniciada.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Autenticacion fallida.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Snackbar.make(getCurrentFocus(),"Asegurate de llenar todos los campos!!",Snackbar.LENGTH_SHORT).show();
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //checar si el usuario ya habia iniciado sesion
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null){//si diferente de nulo entonces si existia una sesion
            //llevar a la otra activity
            startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try{
            GoogleSignInAccount account = task.getResult(ApiException.class);
            //si no hay excepcion significa que se inicio correctamente la sesion
            //mando a la otra activity
            firebaseAuthWithGoogle(account);
        }catch(ApiException e){
            Log.e("Error de login","SignInResult: failed code = "+e.getStatusCode());
            Toast.makeText(getApplicationContext(),"NO SE PUDO INICIAR SESION\n"+e.getStatusCode(),Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.e("Firebase","firebaseAuthWithGoogle: "+account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.e("Sesion","Se inicio");
                            FirebaseUser user = auth.getCurrentUser();
                            //iniciar nueva actividad
                            startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
                            finish();
                        }else{
                            Log.e("Error de login","failed");
                            Toast.makeText(getApplicationContext(),"NO SE PUDO INICIAR SESION\n",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
