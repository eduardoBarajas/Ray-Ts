package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.barajasoft.raites.Adapters.ViewPagerAdapter;
import com.barajasoft.raites.Fragments.ConfirmarViajeFragment;
import com.barajasoft.raites.Fragments.DefinirFechaViajeFragment;
import com.barajasoft.raites.Fragments.DefinirHoraViajeFragment;
import com.barajasoft.raites.Fragments.DefinirTipoViajeFragment;
import com.barajasoft.raites.Fragments.DefinirTrayectoFragment;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.LockableViewPager;

public class PublicarViajeActivity extends BaseActivity implements OnPageChangeListener {
    private LockableViewPager viewPager;
    private View indicator1;
    private View indicator2;
    private View indicator3;
    private View indicator4;
    private View indicator5;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("publicar_viaje");
        setToolbar("","Agendar Viaje");
        View layout = LayoutInflater.from(this).inflate(R.layout.publicar_viaje_activity,null);
        indicator1 = (View) layout.findViewById(R.id.indicator1);
        indicator2 = (View) layout.findViewById(R.id.indicator2);
        indicator3 = (View) layout.findViewById(R.id.indicator3);
        indicator4 = (View) layout.findViewById(R.id.indicator4);
        indicator5 = (View) layout.findViewById(R.id.indicator5);

        viewPager = layout.findViewById(R.id.wizardPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DefinirTrayectoFragment());
        adapter.addFragment(new DefinirTipoViajeFragment());
        adapter.addFragment(new DefinirFechaViajeFragment());
        adapter.addFragment(new DefinirHoraViajeFragment());
        adapter.addFragment(new ConfirmarViajeFragment());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setSwipeable(false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        updateIndicators(0);
        addContent(layout);
    }

    /*private boolean validateDate() {
        int year, month, day;
        year = Integer.parseInt(date[2]);
        month = Integer.parseInt(date[1]);
        day = Integer.parseInt(date[0]);
        String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
        String[] fecha = currentDate.split("/");
        return ((Integer.parseInt(fecha[0]+Integer.parseInt(fecha[1])+1+Integer.parseInt(fecha[2].split("-")[0])))>=(year+month+day));
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateIndicators(int position) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int resizeValue = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
        int defaultValue = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 15, metrics);
        switch (position) {
            case 0:
                indicator1.getLayoutParams().height = resizeValue;
                indicator1.getLayoutParams().width = resizeValue;
                indicator1.requestLayout();

                indicator2.getLayoutParams().height = defaultValue;
                indicator2.getLayoutParams().width = defaultValue;
                indicator2.requestLayout();

                indicator3.getLayoutParams().height = defaultValue;
                indicator3.getLayoutParams().width = defaultValue;
                indicator3.requestLayout();

                indicator4.getLayoutParams().height = defaultValue;
                indicator4.getLayoutParams().width = defaultValue;
                indicator4.requestLayout();

                indicator5.getLayoutParams().height = defaultValue;
                indicator5.getLayoutParams().width = defaultValue;
                indicator5.requestLayout();

                break;

            case 1:
                indicator1.getLayoutParams().height = defaultValue;
                indicator1.getLayoutParams().width = defaultValue;
                indicator1.requestLayout();

                indicator2.getLayoutParams().height = resizeValue;
                indicator2.getLayoutParams().width = resizeValue;
                indicator2.requestLayout();

                indicator3.getLayoutParams().height = defaultValue;
                indicator3.getLayoutParams().width = defaultValue;
                indicator3.requestLayout();

                indicator4.getLayoutParams().height = defaultValue;
                indicator4.getLayoutParams().width = defaultValue;
                indicator4.requestLayout();

                indicator5.getLayoutParams().height = defaultValue;
                indicator5.getLayoutParams().width = defaultValue;
                indicator5.requestLayout();
                break;

            case 2:
                indicator1.getLayoutParams().height = defaultValue;
                indicator1.getLayoutParams().width = defaultValue;
                indicator1.requestLayout();

                indicator2.getLayoutParams().height = defaultValue;
                indicator2.getLayoutParams().width = defaultValue;
                indicator2.requestLayout();

                indicator3.getLayoutParams().height = resizeValue;
                indicator3.getLayoutParams().width = resizeValue;
                indicator3.requestLayout();

                indicator4.getLayoutParams().height = defaultValue;
                indicator4.getLayoutParams().width = defaultValue;
                indicator4.requestLayout();

                indicator5.getLayoutParams().height = defaultValue;
                indicator5.getLayoutParams().width = defaultValue;
                indicator5.requestLayout();
                break;

            case 3:
                indicator1.getLayoutParams().height = defaultValue;
                indicator1.getLayoutParams().width = defaultValue;
                indicator1.requestLayout();

                indicator2.getLayoutParams().height = defaultValue;
                indicator2.getLayoutParams().width = defaultValue;
                indicator2.requestLayout();

                indicator3.getLayoutParams().height = defaultValue;
                indicator3.getLayoutParams().width = defaultValue;
                indicator3.requestLayout();

                indicator4.getLayoutParams().height = resizeValue;
                indicator4.getLayoutParams().width = resizeValue;
                indicator4.requestLayout();

                indicator5.getLayoutParams().height = defaultValue;
                indicator5.getLayoutParams().width = defaultValue;
                indicator5.requestLayout();
                break;
            case 4:
                indicator1.getLayoutParams().height = defaultValue;
                indicator1.getLayoutParams().width = defaultValue;
                indicator1.requestLayout();

                indicator2.getLayoutParams().height = defaultValue;
                indicator2.getLayoutParams().width = defaultValue;
                indicator2.requestLayout();

                indicator3.getLayoutParams().height = defaultValue;
                indicator3.getLayoutParams().width = defaultValue;
                indicator3.requestLayout();

                indicator4.getLayoutParams().height = defaultValue;
                indicator4.getLayoutParams().width = defaultValue;
                indicator4.requestLayout();

                indicator5.getLayoutParams().height = resizeValue;
                indicator5.getLayoutParams().width = resizeValue;
                indicator5.requestLayout();
                break;
        }

    }
    @Override
    public void finish() {
        super.finish();
        clearPreviousPublicationIntent();
    }

    private void clearPreviousPublicationIntent() {
        editor.remove("direccionInicio");
        editor.remove("direccionDestino");
        editor.remove("latitudInicio");
        editor.remove("latitudDestino");
        editor.remove("longitudInicio");
        editor.remove("longitudDestino");
        editor.remove("pageOneCompleted");
        editor.remove("typeSelected");
        editor.remove("roomSelected");
        editor.remove("HoraSeleccionada");
        editor.remove("FechaSeleccionada");
        editor.remove("FechaPublicada");
        editor.remove("roomSelected");
        editor.commit();
    }

    @Override
    public void pageChanged(int position) {
        viewPager.setCurrentItem(position);
    }
}
