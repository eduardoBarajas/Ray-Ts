package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Entities.User;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseActivity {
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private final int SIGN_IN = 751;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Usuarios");

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
        setToolbar("","Login");
        View layout = LayoutInflater.from(this).inflate(R.layout.login_activity,null);
        TextInputEditText user, pass;
        user = layout.findViewById(R.id.txtEmail);
        pass = layout.findViewById(R.id.txtPass);
        Button btnInicioSesion = layout.findViewById(R.id.btnInicioSesion);
        Button btnInicioSesionGoogle = layout.findViewById(R.id.btnInicioGoogle);
        Button btnCrearCuenta = layout.findViewById(R.id.btnCrearCuenta);
        btnCrearCuenta.setOnClickListener(e->{
            startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
        });

        btnInicioSesionGoogle.setOnClickListener(e->{
            signIn();
        });
        btnInicioSesion.setOnClickListener(e->{
            signIn(user.getText().toString(), pass.getText().toString());
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
                                //cargar los datos en la sesion
                                loadSesion(auth.getCurrentUser());
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
            loadSesion(currentUser);
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
                            FirebaseUser usuario = auth.getCurrentUser();
                            //cargar los datos en la sesion
                            referencia.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean found = false;
                                    for(DataSnapshot data : dataSnapshot.getChildren()){
                                        if(data.getValue(User.class).getCorreo().equals(usuario.getEmail()))
                                            found = true;
                                    }
                                    if(!found){
                                        User nuevoUsuario = new User();
                                        nuevoUsuario.setNombre(usuario.getDisplayName());
                                        nuevoUsuario.setTelefono(usuario.getPhoneNumber());
                                        if(nuevoUsuario.getTelefono() == null)
                                            nuevoUsuario.setTelefono("");
                                        nuevoUsuario.setCorreo(usuario.getEmail());
                                        nuevoUsuario.setImagenPerfil(usuario.getPhotoUrl().toString());
                                        nuevoUsuario.setRating(3);
                                        referencia.child(nuevoUsuario.getKey()).setValue(nuevoUsuario, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                loadSesion(auth.getCurrentUser());
                                            }
                                        });
                                    }else{
                                        loadSesion(auth.getCurrentUser());
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }else{
                            Log.e("Error de login","failed");
                            Toast.makeText(getApplicationContext(),"NO SE PUDO INICIAR SESION\n",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loadSesion(FirebaseUser user) {
        referencia.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User dbUser = dataSnapshot.getValue(User.class);
                if(dbUser.getCorreo().equals(user.getEmail())){
                    setUserSesionData(dbUser);
                    setUserVehiculoFromKey(dbUser.getKey());
                    Toast.makeText(LoginActivity.this, "Sesion iniciada.", Toast.LENGTH_SHORT).show();
                    referencia.removeEventListener(this);
                    startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
                    finish();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
