package com.example.cft_test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.cft_test.databinding.ActivityMainBinding;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private final int UPDATE_DELAY = 65000;

    ActivityMainBinding binding;
    MainActivityViewModel model;

    List<String> valutes = new ArrayList<>();

    RecyclerView recyclerView;
    ValuteListAdapter adapter;
    Valute valute;

    Runnable runnable;
    Handler handler = new Handler(Looper.getMainLooper());

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        model = new ViewModelProvider(this).get(MainActivityViewModel.class);
        binding.setModel(model);


        model.updateValutesWithDateCheck(this, queue);

        setLocale();

        valute = new Valute(model.getChosenValuteID(), model.getCharCode(), model.getNominal(), model.getName(), model.getValue(), model.getRublesAmount(), binding.getLocale());
        binding.setValute(valute);

        setRV();

        setValuteTIL();

        binding.rublesTIET.addTextChangedListener(new CurrencyTextWatcher(binding));

        binding.swipeRefresh.setOnRefreshListener(() -> model.updateValutesWithoutDateCheck(binding.getRoot().getContext(), queue));

        refillValutes();


    }

    private void setLocale() {
        if (Locale.getDefault().toString().equals("ru_RU")) {
            binding.setLocale(Locale.getDefault());
        } else {
            binding.setLocale(Locale.US);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            model.updateValutesWithoutDateCheck(binding.getRoot().getContext(), queue);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setValuteTIL() {

        binding.valuteTIL.setEndIconOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);

            for (String str : valutes) {
                if (!str.equals(valute.getName())) popupMenu.getMenu().add(str);
            }

            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(item -> {
                model.setRublesAmount(valute.getRublesAmount());
                setValute(item.getTitle().toString());

                return false;
            });
        });
    }

    private void setValute(String chosenValute) {
        model.setChosenValutebyName(chosenValute);

        valute = binding.getValute();
        valute.setID(model.getChosenValuteID());
        valute.setValue(model.getValue());
        valute.setCharCode(model.getCharCode());
        valute.setName(model.getName());
        valute.setNominal(model.getNominal());
        valute.setRublesAmount(model.getRublesAmount());

        setValuteTIET();

        setRecyclerViewSelection(chosenValute);
    }

    private void setRecyclerViewSelection(String chosenValute) {
        int oldPos = adapter.selectedPos;
        adapter.selectedPos = valutes.indexOf(chosenValute);

        adapter.notifyItemChanged(oldPos);
        adapter.notifyItemChanged(adapter.selectedPos);

        recyclerView.smoothScrollToPosition(valutes.indexOf(chosenValute));
    }

    public void setValuteTIET() {

        Valute valute = binding.getValute();

        NumberFormat format = NumberFormat.getInstance(binding.getLocale());

        try {
            valute.setValuteAmount(String.format(binding.getLocale(), "%,.2f", (Objects.requireNonNull(format.parse(valute.getRublesAmount())).doubleValue() * valute.getNominal() / valute.getValue())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (valute.getValuteAmount().equals("NaN")) valute.setValuteAmount("");
    }

    @Override
    protected void onResume() {

        setUpdateDataByTimer();

        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void setUpdateDataByTimer() {

        handler.postDelayed(runnable = () -> {
            model.updateValutesWithDateCheck(binding.getRoot().getContext(), queue);
            handler.postDelayed(runnable, UPDATE_DELAY);
        }, UPDATE_DELAY);

    }

    private void setRV() {
        recyclerView = binding.valutesRv;

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        valutes.addAll(model.getValutes());
        adapter = new ValuteListAdapter(this, valutes);

        adapter.setClickListener((view, position) -> {
            model.setRublesAmount(valute.getRublesAmount());
            setValute(valutes.get(position));
        });

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public void refillValutes() {

        valutes.clear();
        valutes.addAll(model.getValutes());

        adapter.notifyDataSetChanged();

        if (valute != null) {
            if (valute.getValuteAmount().equals("") && valutes.size() > 0) {
                if (valute.getName() == null) {
                    setValute(valutes.get(0));
                } else {
                    setValute(valute.getName());
                }
            }
        }
    }

    public void stopRefresh() {
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        model.setRublesAmount(valute.getRublesAmount());

    }
}