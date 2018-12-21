package com.barajasoft.raites.Activities;

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

import com.barajasoft.raites.Adapters.ViewPagerAdapter;
import com.barajasoft.raites.Fragments.AvaibleRidesFragment;
import com.barajasoft.raites.Fragments.MapFragment;
import com.barajasoft.raites.Fragments.MyRideFragment;
import com.barajasoft.raites.R;

import static android.view.KeyEvent.KEYCODE_BACK;

public class BaseActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private NavigationView navItems;
    private BottomNavigationView bottomNavigation;
    private ViewPager viewPager;
    private MenuItem prevMenuItem = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);

        initDrawer();
        initViewPager();

    }

    private void initViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment());
        adapter.addFragment(new MyRideFragment());
        adapter.addFragment(new AvaibleRidesFragment());
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

    private void initDrawer() {
        drawerLayout = findViewById(R.id.drawer);
        drawer = findViewById(R.id.drawer_layout);
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
                break;
        }
        navItems.bringToFront();
    }

    protected void disableBottomMenu(){
        bottomNavigation.setVisibility(View.GONE);
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
                case R.id.option3:
                    viewPager.setCurrentItem(2);
                    break;
            }
            return false;
        });
    }
}
