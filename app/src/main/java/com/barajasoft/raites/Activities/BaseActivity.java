package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.barajasoft.raites.Adapters.ViewPagerAdapter;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Vehiculo;
import com.barajasoft.raites.Fragments.BuscarViajesFragment;
import com.barajasoft.raites.Fragments.DetallesViajeFragment;
import com.barajasoft.raites.Fragments.MapFragment;
import com.barajasoft.raites.Fragments.ViajesActivosFragment;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.LockableViewPager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import static android.view.KeyEvent.KEYCODE_BACK;

public class BaseActivity extends AppCompatActivity implements OnPageChangeListener {
    protected static final int PICK_IMAGE = 765;
    protected static final int PICK_DIRECCION = 123;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private NavigationView navItems;
    private BottomNavigationView bottomNavigation;
    private LockableViewPager viewPager;
    private MenuItem prevMenuItem = null;
    private LinearLayout contentLayout;
    private OnPageChangeListener onPageChangeListener;
    private Toolbar toolbar;
    private ViewPagerAdapter adapter;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private DatabaseReference vehiculosReference = database.getReference("Vehiculos");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onPageChangeListener = this;
        setContentView(R.layout.base_activity);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.clientOath))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,gso);
        contentLayout = findViewById(R.id.contentLinearLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.navigation);
        drawerLayout = findViewById(R.id.drawer);
        drawer = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();

        usuariosReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User currentUsuario = dataSnapshot.getValue(User.class);
                if(currentUsuario.getCorreo().equals(pref.getString("correo",null)))
                    setUserSesionData(currentUsuario);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        vehiculosReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Vehiculo currentVehiculo = dataSnapshot.getValue(Vehiculo.class);
                if(currentVehiculo.getUserKey().equals(pref.getString("key",null)))
                    setVehiculoSesionData(currentVehiculo);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    protected void setUserVehiculoFromKey(String key){
        vehiculosReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean found = false;
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.getValue(Vehiculo.class).getUserKey().equals(key)){
                        setVehiculoSesionData(data.getValue(Vehiculo.class));
                        found = true;
                        break;
                    }
                }
                if(!found){
                    deleteVehiculoSesion();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    protected void setVehiculoSesionData(Vehiculo sesion){
        editor.putString("matricula",sesion.getMatricula());
        editor.putString("modelo",sesion.getModelo());
        editor.putString("marca",sesion.getMarca());
        editor.putInt("espaciosDisponibles",sesion.getEspaciosDisponibles());
        editor.putString("keyVehiculo",sesion.getKey());
        editor.putString("keyUsuarioVehiculo",sesion.getUserKey());
        editor.putString("imageVehiculoLink",sesion.getImageLink());
        editor.commit();
        if(validarVehiculo())
            editor.putBoolean("vehiculoValidado", true);
        else
            editor.putBoolean("vehiculoValidado", false);
        editor.commit();
        Map<String, Object> datoUsuario = new HashMap<>(), datoVehiculo = new HashMap<>();
        if(pref.getBoolean("vehiculoValidado", false) != sesion.isValidado()){
            datoUsuario.put("validadoConductor", pref.getBoolean("vehiculoValidado", false));
            datoVehiculo.put("validado", pref.getBoolean("vehiculoValidado", false));
        }
        if(datoUsuario.size() > 0)
            usuariosReference.child(pref.getString("key", null)).updateChildren(datoUsuario);
        if(datoVehiculo.size() > 0)
            vehiculosReference.child(sesion.getKey()).updateChildren(datoVehiculo);
        editor.putBoolean("carAdded",true);
        editor.commit();
        update();
    }

    private boolean validarVehiculo() {
        if(!pref.getString("matricula", null).isEmpty()&&!pref.getString("modelo", null).isEmpty()
                &&!pref.getString("marca", null).isEmpty()&&pref.getInt("espaciosDisponibles", -1) > 0){
            return true;
        }else{
            return false;
        }
    }

    protected void setUserSesionData(User sesion){
        editor.putString("correo",sesion.getCorreo());
        editor.putInt("edad",sesion.getEdad());
        editor.putString("linkPerfil",sesion.getImagenPerfil());
        editor.putString("key",sesion.getKey());
        editor.putString("nombre",sesion.getNombre());
        editor.putFloat("rating",sesion.getRating());
        editor.putString("sexo",sesion.getSexo());
        editor.putString("telefono",sesion.getTelefono());
        editor.commit();
        if(validarUsuario())
            editor.putBoolean("validadoPasajero", true);
        else
            editor.putBoolean("validadoPasajero", false);
        if(pref.contains("vehiculoValidado")&&pref.getBoolean("vehiculoValidado", false))
            editor.putBoolean("validadoConductor", true);
        else
            editor.putBoolean("validadoConductor", false);
        editor.commit();
        Map<String, Object> datos = new HashMap<>();
        if(sesion.isValidadoConductor() != pref.getBoolean("validadoConductor", false))
            datos.put("validadoConductor", pref.getBoolean("validadoConductor", false));
        if(sesion.isValidadoPasajero() != pref.getBoolean("validadoPasajero", false))
            datos.put("validadoPasajero", pref.getBoolean("validadoPasajero", false));
        if(datos.size()>0){
            usuariosReference.child(sesion.getKey()).updateChildren(datos);
        }
        editor.commit();
        update();
    }

    private boolean validarUsuario() {
        if(!pref.getString("correo", null).isEmpty() && pref.getInt("edad", -1) > 0 && !pref.getString("linkPerfil", null).isEmpty()
                && !pref.getString("key", null).isEmpty()&&!pref.getString("nombre", null).isEmpty()&&pref.getFloat("rating", -1) > 0
                &&!pref.getString("sexo", null).isEmpty()&&!pref.getString("telefono", null).isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    protected void initViewPager(String activity) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        switch (activity){
            case "MainMenu":
                ViajesActivosFragment f = new ViajesActivosFragment();
                f.setListener(onPageChangeListener);
                adapter.addFragment(f);
                adapter.addFragment(new BuscarViajesFragment());
                break;
            case "ExpandedViaje":
                DetallesViajeFragment f2 = new DetallesViajeFragment();
                f2.setListener(onPageChangeListener);
                adapter.addFragment(f2);
                adapter.addFragment(new MapFragment());
                break;
        }
        viewPager.setAdapter(adapter);
        viewPager.setSwipeable(false);
        /*
            Se agrego un on page listener, para que se pudiera hacer el cambio de los fragments segun la opcion
            seleccionada del bottomview.
         */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                if(prevMenuItem != null)
                    prevMenuItem.setChecked(false);
                else
                    bottomNavigation.getMenu().getItem(0).setChecked(false);
                bottomNavigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigation.getMenu().getItem(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    protected void initDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);
        navItems =  drawer.findViewById(R.id.navItems);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void openDrawer(){drawerLayout.openDrawer(GravityCompat.START);}

    public boolean onKeyDown(int keyCode, KeyEvent event){
        //si se recibe una tecla se limpia la pantalla
        super.onKeyDown(keyCode,event);
        if(keyCode == KEYCODE_BACK){
            finish();
        }
        return true;
    }

    protected void setNavViewMenu(String options){
        navItems.getMenu().clear();
        navItems.inflateMenu(R.menu.main_menu_menu);
        setDrawerOptionsListener(navItems,options);
        navItems.bringToFront();
    }

    private void setDrawerOptionsListener(NavigationView nav, String current) {
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.inicio:
                        if(!current.equals("inicio")) {
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en el inicio",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.publicar_viaje:
                        if(!current.equals("publicar_viaje")) {
                            startActivity(new Intent(BaseActivity.this, PublicarViajeActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas publicando un viaje",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.perfil:
                        if(!current.equals("perfil")) {
                            startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en el perfil",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.miVehiculo:
                        if(!current.equals("miVehiculo")) {
                            startActivity(new Intent(BaseActivity.this, MiVehiculoActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en tu vehiculo",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.acerca_nosotros:
                        if(!current.equals("acerca_nosotros")) {
                            startActivity(new Intent(BaseActivity.this, AboutActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en acerca de la app",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.cerrar_sesion:
                        if(!current.equals("cerrar_sesion")) {
                            if(signOut()) {
                                startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                        break;
                    case R.id.contactanos:
                        if(!current.equals("contactanos")) {
                            startActivity(new Intent(BaseActivity.this, ContactActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en contacto",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.ayuda:
                        if(!current.equals("ayuda")) {
                            startActivity(new Intent(BaseActivity.this, HelpActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en ayuda",Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    protected void disableBottomMenu(){
        bottomNavigation.setVisibility(View.GONE);
    }

    protected void disableDrawer(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    protected void disableViewPager(){
        viewPager.setVisibility(View.GONE);
    }

    protected void setBottomMenu(String options){
        bottomNavigation.getMenu().clear();
        switch (options){
            case "MainMenu":
                bottomNavigation.inflateMenu(R.menu.main_menu_bottom_menu);
                break;
            case "ExpandedViaje":
                bottomNavigation.inflateMenu(R.menu.expanded_viaje_bottom_menu);
                break;
        }
        /*
            El onNavigationItemSelectedListener se configura con option[indice] para que pueda ser reutilizado
            en caso de que se quiera cambiar el numero de opciones de menu, el menu no deberia tener mas de 3 opciones..
         */
        bottomNavigation.setOnNavigationItemSelectedListener(e->{
            switch (e.getItemId()){
                case R.id.option1:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.option2:
                    viewPager.setCurrentItem(1);
                    break;
            }
            return false;
        });
    }

    protected void addContent(View content){
        contentLayout.addView(content);
    }

    protected boolean signOut(){
        auth.signOut();
        googleSignInClient.signOut();
        editor.clear();
        editor.commit();
        return true;
    }

    protected void deleteVehiculoSesion(){
        editor.remove("matricula");
        editor.remove("modelo");
        editor.remove("marca");
        editor.remove("espaciosDisponibles");
        editor.remove("keyVehiculo");
        editor.remove("keyUsuarioVehiculo");
        editor.remove("imageVehiculoLink");
        editor.remove("vehiculoValidado");
        editor.putBoolean("carAdded",false);
        editor.commit();
        update();
    }

    @Override
    public void pageChanged(int position) {
        viewPager.setCurrentItem(position);
        bottomNavigation.getMenu().getItem(position).setChecked(true);
        prevMenuItem = bottomNavigation.getMenu().getItem(position);
    }

    protected void setToolbar(String color, String label){
        if(!color.isEmpty())
            toolbar.setBackgroundColor(Color.parseColor(color));
        toolbar.setSubtitle(label);
    }

    protected  void setToolbarTitle(String title, String sub){
        toolbar.setTitle(title);
        toolbar.setSubtitle(sub);
    }

    protected void disableToolbar(){
        toolbar.setVisibility(View.GONE);
    }

    protected String getCurrentUserKey(){
        return pref.getString("key",null);
    }

    protected ViewPagerAdapter getViewPagerAdapter(){ return adapter; }

    protected boolean isVehiculoAgregado(){
        return pref.getBoolean("carAdded",false);
    }

    protected void update(){ }
}
