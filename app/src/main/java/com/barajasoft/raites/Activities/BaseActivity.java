package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.barajasoft.raites.Fragments.BuscarViajesFragment;
import com.barajasoft.raites.Fragments.ViajesActivosFragment;
import com.barajasoft.raites.Fragments.PublicarViajeFragment;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import static android.view.KeyEvent.KEYCODE_BACK;

public class BaseActivity extends AppCompatActivity implements OnPageChangeListener {
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private NavigationView navItems;
    private BottomNavigationView bottomNavigation;
    private ViewPager viewPager;
    private MenuItem prevMenuItem = null;
    private LinearLayout contentLayout;
    private OnPageChangeListener onPageChangeListener;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    protected void initViewPager(String activity) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        switch (activity){
            case "MainMenu":
                ViajesActivosFragment f = new ViajesActivosFragment();
                f.setListener(onPageChangeListener);
                adapter.addFragment(f);
                adapter.addFragment(new PublicarViajeFragment());
                adapter.addFragment(new BuscarViajesFragment());
                break;
        }
        viewPager.setAdapter(adapter);
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
        switch (options){
            case "MainMenu":
                navItems.inflateMenu(R.menu.main_menu_menu);
                setDrawerOptionsListener(navItems,"inicio");
                break;
        }
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
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas publicando un viaje",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.perfil:
                        if(!current.equals("perfil")) {
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en el perfil",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.miVehiculo:
                        if(!current.equals("miVehiculo")) {
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en tu vehiculo",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.acerca_nosotros:
                        if(!current.equals("acerca_nosotros")) {
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
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
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Ya estas en contacto",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.ayuda:
                        if(!current.equals("ayuda")) {
                            startActivity(new Intent(BaseActivity.this, MainMenuActivity.class));
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
        return true;
    }

    @Override
    public void pageChanged(int position) {
        viewPager.setCurrentItem(position);
        bottomNavigation.getMenu().getItem(position).setChecked(true);
        prevMenuItem = bottomNavigation.getMenu().getItem(position);
    }
}
