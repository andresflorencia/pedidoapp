package com.florencia.pedidossi.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.fragments.HomeFragment;
import com.florencia.pedidossi.models.Empresa;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final int ACTIVITY_CODE = 200;
    private static final int ACTIVITY_LOGIN = 300;
    private static final int ACTIVITY_CARRITO = 400;
    DrawerLayout drawer_layout;
    Toolbar toolbar;
    NavigationView nav_view;
    ActionBarDrawerToggle drawerToggle;
    ImageButton btnCarroToolbar, toolbarSearch;
    public TextView lblCantCarrito, toolbarTitle, toolbarSubtitle, lblNameUser, lblInfoUser;
    Empresa miEmpresa = new Empresa();
    Gson gson;
    SearchView searchView;
    RelativeLayout lyToolbarCarrito;
    HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        lblCantCarrito = findViewById(R.id.toolbarIndicador);
        btnCarroToolbar = findViewById(R.id.toolbarCarrito);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarSubtitle = findViewById(R.id.toolbarSubTitle);
        toolbarSearch = findViewById(R.id.toolbarSearch);
        toolbarSearch.setVisibility(View.VISIBLE);
        lyToolbarCarrito = findViewById(R.id.lyToolbarCarrito);

        lblNameUser = nav_view.getHeaderView(0).findViewById(R.id.lblUsuario);
        lblInfoUser = nav_view.getHeaderView(0).findViewById(R.id.lblInfo);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        gson = new GsonBuilder()
                .setLenient()
                .create();
        if (Global.empresa != null) {
            homeFragment = new HomeFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContent, homeFragment)
                    .commit();
        }

        nav_view.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        lblCantCarrito.setText("0");

        btnCarroToolbar.setOnClickListener(this::onClick);
        toolbarSearch.setOnClickListener(this::onClick);

        VerificaMenu();
    }

    public void VerificaMenu() {
        SubMenu subMenu = nav_view.getMenu().findItem(R.id.menu_cuenta).getSubMenu();
        if (Global.usuario == null) {
            subMenu.findItem(R.id.nav_profile).setTitle(R.string.login);
            toolbarTitle.setText("Inicio");
            lblNameUser.setText("No has iniciado sesión");
        } else {
            subMenu.findItem(R.id.nav_profile).setTitle(R.string.profile);
            toolbarTitle.setText("Hola " + Global.usuario.nombres + ",");
            subMenu.findItem(R.id.nav_logout).setVisible(true);
            nav_view.getMenu().findItem(R.id.nav_history).setVisible(true);
            lblNameUser.setText(Global.usuario.toString());
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent i;
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                if(homeFragment == null)
                    homeFragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, homeFragment).commit();
                break;
            case R.id.nav_cart_shop:
                if(Global.carrito == null) {
                    MyToast.show(HomeActivity.this, "No hay productos en el carrito", MyToast.SHORT, MyToast.ERROR);
                    return true;
                }
                i = new Intent(HomeActivity.this, CarritoActivity.class);
                startActivity(i);
                break;
            case R.id.nav_history:
                if(Global.usuario == null)
                    MyToast.show(HomeActivity.this, "No has iniciado sesión...", MyToast.SHORT, MyToast.ERROR);
                else{
                    i = new Intent(HomeActivity.this, HistoryActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.nav_profile:
                if (Global.usuario == null) {
                    i = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivityForResult(i, ACTIVITY_LOGIN);
                } else {
                    i = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivityForResult(i, ACTIVITY_CODE);
                }
                break;
            case R.id.nav_logout:
                if (Global.usuario.CerrarSesionLocal(HomeActivity.this)) {
                    Global.usuario = null;
                    /*SubMenu subMenu = nav_view.getMenu().findItem(R.id.menu_cuenta).getSubMenu();
                    subMenu.findItem(R.id.nav_profile).setTitle(R.string.login);
                    toolbarTitle.setText("Inicio");
                    subMenu.findItem(R.id.nav_logout).setVisible(false);*/
                    VerificaMenu();
                    MyToast.show(HomeActivity.this, "Has cerrado sesión. Regresa pronto...", MyToast.LONG, MyToast.ERROR);
                }
                break;
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarCarrito:
                if (Global.carrito == null || Global.carrito.detalle.size() <= 0) {
                    MyToast.show(HomeActivity.this, "No hay productos en el carrito.", MyToast.SHORT, MyToast.ERROR);
                    return;
                } else {
                    Intent i = new Intent(v.getContext(), CarritoActivity.class);
                    startActivityForResult(i, ACTIVITY_CARRITO);
                }
                break;
            case R.id.toolbarSearch:
                toolbar.getMenu().findItem(R.id.action_search).setVisible(true);
                searchView.setVisibility(View.VISIBLE);
                searchView.setIconified(false);
                showToolbar(false);
                break;
        }
    }

    private void showToolbar(boolean show) {
        if (show) {
            toolbarTitle.setVisibility(View.VISIBLE);
            toolbarSubtitle.setVisibility(View.VISIBLE);
            toolbarSearch.setVisibility(View.VISIBLE);
            //btnCarroToolbar.setVisibility(View.VISIBLE);
            //lblCantCarrito.setVisibility(View.VISIBLE);
            //lyToolbarCarrito.setVisibility(View.VISIBLE);
        } else {
            toolbarTitle.setVisibility(View.GONE);
            toolbarSubtitle.setVisibility(View.GONE);
            toolbarSearch.setVisibility(View.GONE);
            //btnCarroToolbar.setVisibility(View.GONE);
            //lblCantCarrito.setVisibility(View.GONE);
            //lyToolbarCarrito.setVisibility(View.GONE);
        }
        //toolbarSearch.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Global.carrito != null)
            lblCantCarrito.setText("" + Global.carrito.detalle.size());
        if (Global.empresa != null) {
            toolbarSubtitle.setText(Global.empresa.nombre);
            toolbarSubtitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_LOGIN:
                    /*if (Global.usuario != null) {
                        SubMenu subMenu = nav_view.getMenu().findItem(R.id.menu_cuenta).getSubMenu();
                        subMenu.findItem(R.id.nav_profile).setTitle(R.string.profile);
                        subMenu.findItem(R.id.nav_logout).setVisible(true);
                        nav_view.getMenu().findItem(R.id.nav_history).setVisible(true);
                        toolbarTitle.setText("Hola " + Global.usuario.nombres + ",");
                        MyToast.show(HomeActivity.this, "Bienvenido " + Global.usuario.nombres + "...", MyToast.LONG, MyToast.SUCCESS);
                    }*/
                    VerificaMenu();
                    MyToast.show(HomeActivity.this, "Bienvenido " + Global.usuario.nombres + "...", MyToast.LONG, MyToast.SUCCESS);
                    break;
                case ACTIVITY_CARRITO:
                    lblCantCarrito.setText("0");
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setQueryHint("Búsqueda de productos");
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();
        searchView.setVisibility(View.GONE);
        searchView.setOnCloseListener(
                () -> {
                    showToolbar(true);
                    searchView.setVisibility(View.GONE);
                    searchItem.setVisible(false);
                    return false;
                }
        );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
                if(!query.trim().equals("") && homeFragment != null)
                    homeFragment.cargarProductos(Global.empresa.rucempresa, query.trim().toLowerCase());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });
        View searchPlate = (View) searchView.findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setBackgroundResource(R.drawable.bg_border_black3);

        return super.onCreateOptionsMenu(menu);
    }
}
